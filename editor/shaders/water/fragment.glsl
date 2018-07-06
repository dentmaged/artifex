#version 330

in mat4 viewModelMatrix;
in vec4 worldPosition;
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

uniform mat4 inverseProjectionMatrix;
uniform mat4 projectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 normalMatrix;

/*
uniform float minDiffuse;
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightColour[MAX_LIGHTS];

const int pcfCount = 4;
const int totalTexels = (pcfCount * 2 + 1) * (pcfCount * 2 + 1);
const float texelSize = 1.0 / 2048.0;
*/

const float reflectivity = 1;
const float shininess = 20;

uniform vec4 colour;

#include "util.glsl"
#include "functions.glsl"
#include "sky.glsl"
#include "reflection.glsl"
#include "lighting.glsl"

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
/*
	vec3 unitVectorToCamera = normalize(-viewPosition.xyz);
	vec3 F0 = vec3(0.15);
	F0 = mix(F0, colour.xyz, colour.w);
	vec3 fresnel = fresnel(max(dot(unitVectorToCamera, unitNormal), 0), F0);

	vec3 reflected = normalize(reflect(-unitVectorToCamera, unitNormal));
	vec3 reflectColour = getReflection(viewPosition.xyz, reflected);

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

		vec3 reflectedLightDirection = reflect(-unitLightVector, unitNormal);
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera) * 0.5 + 0.5;
		float dampedFactor = pow(specularFactor, shininess);

		totalDiffuse += brightness * lightColour[i] / attFactor;
		totalSpecular += dampedFactor * reflectivity * lightColour[i] / attFactor;
	}
	totalDiffuse = max(totalDiffuse, minDiffuse);
	totalSpecular *= depthReduction;

	out_bloom = vec4(0, 0, 0, 1);
	out_godrays = vec4(0, 0, 0, 1);

	if (!gl_FrontFacing) {
		out_diffuse = vec4(texture2D(scene, clamp(ndc + totalDistortion, 0.001, 0.999)).xyz * fresnel * totalDiffuse, 1);
		out_bloom.a = dot(unitVectorToCamera, unitNormal) * 2;
		out_godrays.a = out_bloom.a;
	} else {
		out_diffuse = vec4(totalDiffuse * reflectColour * fresnel, 1) + vec4(totalSpecular, 0);
		out_diffuse = mix(vec4(baseColour, 1), out_diffuse, visibility);
		out_diffuse.a = depthReduction;
	}
	*/
	out_diffuse = vec4(performLighting(viewPosition.xyz, unitNormal, vec3(1), 1, 0.5, 0, 1), 1);
}
