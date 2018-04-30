/*
This software includes code from Emerald Engine:

MIT License

Copyright (c) 2018 Lage Ragnarsson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
#version 330

in mat4 viewModelMatrix;
in vec4 viewPosition;
in vec4 clipSpace;
in vec4 shadowCoords;
in vec2 tc;
in float visibility;

FS_OUT(diffuse)
FS_OUT(bloom)
FS_OUT(godrays)

tex scene;
tex depthMap;
tex dudvMap;
tex normalMap;
tex shadowMap;

uniform float near;
uniform float far;
uniform float moveFactor;
uniform float height;
uniform float waveStrength;
uniform float minDiffuse;

uniform mat4 inverseProjectionMatrix;
uniform mat4 projectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 normalMatrix;

uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightColour[MAX_LIGHTS];

uniform vec4 colour;
uniform vec3 baseColour;
uniform vec3 topColour;

const float reflectivity = 1;
const float shineDamper = 20;

const float step = 0.1;
const float minRayStep = 0.1;
const float maxSteps = 30;
const int numBinarySearchSteps = 5;

const int pcfCount = 4;
const int totalTexels = (pcfCount * 2 + 1) * (pcfCount * 2 + 1);
const float texelSize = 1.0 / 2048.0;

vec3 getPosition(vec2 coords) {
	vec3 raw = vec3(coords, texture2D(depthMap, coords).r);
	vec4 ssp = vec4(raw * 2 - 1, 1);
	vec4 view = inverseProjectionMatrix * ssp;

	return view.xyz / view.w;
}

vec3 binarySearch(inout vec3 dir, inout vec3 hitCoord, inout float dDepth) {
	float depth;
	vec4 projectedCoord;

	int i;
	for (i = 0; i < numBinarySearchSteps; i++) {
		projectedCoord = projectionMatrix * vec4(hitCoord, 1);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getPosition(projectedCoord.xy).z;
		dDepth = hitCoord.z - depth;
		dir *= 0.5;
		if (dDepth > 0)
			hitCoord += dir;
		else
			hitCoord -= dir;
	}

	projectedCoord = projectionMatrix * vec4(hitCoord, 1);
	projectedCoord.xy /= projectedCoord.w;
	projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

	return vec3(projectedCoord.xy, depth);
}

vec4 raymarch(vec3 dir, inout vec3 hitCoord, out float dDepth) {
	dir *= step;
	float depth;
	int steps;
	vec4 projectedCoord;

	for (int i = 0; i < maxSteps; i++) {
		hitCoord += dir;
		projectedCoord = projectionMatrix * vec4(hitCoord, 1);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getPosition(projectedCoord.xy).z;
		if (depth > 1000)
			continue;

		dDepth = hitCoord.z - depth;
		if (dir.z - dDepth < 1.2) {
			if (dDepth <= 0) {
				return vec4(binarySearch(dir, hitCoord, dDepth), 1);
			}
		}

		steps++;
	}

	return vec4(projectedCoord.xy, depth, 0);
}

void main(void) {
	vec2 ndc = (clipSpace.xy / clipSpace.w) * 0.5 + 0.5;
	float floorDistance = 2 * near * far / (far + near - (2 * texture2D(depthMap, ndc).x - 1) * (far - near));
	float surfaceDistance = 2 * near * far / (far + near - (2 * gl_FragCoord.z - 1) * (far - near));
	float waterDepth = floorDistance - surfaceDistance;
	float depthReduction = clamp(waterDepth / 3, 0, 1);

	vec2 distortedTexCoords = texture2D(dudvMap, vec2(tc.x + moveFactor, tc.y)).xy * 0.1;
	distortedTexCoords = tc + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
	vec2 totalDistortion = (texture2D(dudvMap, distortedTexCoords).xy * 2 - 1) * waveStrength * depthReduction;

	vec4 normalMapColour = texture2D(normalMap, distortedTexCoords);
	vec3 localNormal = vec3(normalMapColour.r * 2 - 1, normalMapColour.b * 3, normalMapColour.g * 2 - 1);
	vec3 unitNormal = normalize(mat3(normalMatrix) * localNormal);

	vec3 unitVectorToCamera = normalize(-viewPosition.xyz);
	vec3 F0 = vec3(0.15);
	F0 = mix(F0, colour.xyz, colour.w);
	vec3 fresnel = F0 + (1 - F0) * pow(1 - max(dot(unitVectorToCamera, unitNormal), 0), 5);

	vec3 reflected = normalize(reflect(normalize(viewPosition.xyz), unitNormal));
	vec3 hitPos = viewPosition.xyz;
	float dDepth;
	vec4 coords = raymarch(reflected * max(minRayStep, -viewPosition.z), hitPos, dDepth);

	vec2 dCoords = smoothstep(0.2, 0.6, abs(vec2(0.5) - coords.xy));
	float screenEdgeFactor = clamp(1 - (dCoords.x + dCoords.y), 0, 1);
	float factor = clamp(screenEdgeFactor * -reflected.z, 0, 1);
	reflected = normalize((inverseViewMatrix * vec4(reflected, 0)).xyz);

	vec3 calculatedReflectColour = mix(baseColour, topColour, max(dot(reflected, vec3(0, 1, 0)), 0));
	calculatedReflectColour.xyz = pow(calculatedReflectColour.xyz, vec3(GAMMA));
	vec3 reflectColour = mix(calculatedReflectColour, texture2D(scene, coords.xy).xyz, factor);

	vec3 totalDiffuse = vec3(0);
	vec3 totalSpecular = vec3(0);

	for (int i = 0; i < MAX_LIGHTS; i++) {
		if (dot(lightColour[i], lightColour[i]) == 0)
			continue;

		vec3 toLightVector = lightPosition[i] - viewPosition.xyz;
		vec3 unitLightVector = normalize(toLightVector);
		float distance = length(toLightVector);

		float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
		float nDotl = dot(unitNormal, unitLightVector);
		float brightness = max(nDotl, 0);

		vec3 lightDirection = -unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
		specularFactor = max(specularFactor, 0);

		float dampedFactor = pow(specularFactor, shineDamper);
		totalDiffuse += brightness * lightColour[i] / attFactor;
		totalSpecular += dampedFactor * reflectivity * lightColour[i] / attFactor;
	}
	totalDiffuse = max(totalDiffuse, minDiffuse);
	totalSpecular *= depthReduction;

	out_bloom = vec4(0, 0, 0, 0);
	out_godrays = vec4(0, 0, 0, 0);

	if (!gl_FrontFacing) {
		out_diffuse = vec4(texture2D(scene, clamp(ndc + totalDistortion, 0.001, 0.999)).xyz * fresnel * totalDiffuse, 1);
		out_bloom.a = dot(unitVectorToCamera, unitNormal) * 2;
		out_godrays.a = out_bloom.a;
	} else {
		out_diffuse = vec4(totalDiffuse * reflectColour * fresnel, 1) + vec4(totalSpecular, 0);
		out_diffuse = mix(vec4(baseColour, 1), out_diffuse, visibility);
		out_diffuse.a = depthReduction;
	}
}
