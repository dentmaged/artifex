#version 330

in vec2 tc;

FS_OUT(diffuse)
FS_OUT(position)
FS_OUT(normal)
FS_OUT(bloom)

tex diffuse;
tex position;
tex normal;
tex extra;
tex shadowMap;
tex ssao;

uniform mat4 inverseViewMatrix;

uniform float minDiffuse;
uniform float density;
uniform float gradient;
uniform vec3 skyColour;

uniform mat4 toShadowMapSpace;
uniform float shadowDistance;

uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightColour[MAX_LIGHTS];

uniform bool showLightmaps;

const float transitionDistance = 3;
const int pcfCount = 4;
const int totalTexels = (pcfCount * 2 + 1) * (pcfCount * 2 + 1);
const float texelSize = 1.0 / 2048.0;

void main(void) {
	vec4 diffuse = texture2D(diffuse, tc);
	vec4 position = texture2D(position, tc);
	vec4 normal = texture2D(normal, tc);

	float shineDamper = diffuse.a;
	float reflectivity = normal.a;

	vec3 viewPosition = position.xyz;
	vec3 toCameraVector = -viewPosition;
	float distance = length(toCameraVector);

	vec3 unitVectorToCamera = normalize(toCameraVector);
	vec3 unitNormal = normalize(normal.xyz);

	vec3 totalDiffuse = vec3(0);
	vec3 totalSpecular = vec3(0);

	vec4 shadowCoords = toShadowMapSpace * inverseViewMatrix * position;
	float shadowCoordinatesDistance = distance - (shadowDistance - transitionDistance);
	shadowCoordinatesDistance = shadowCoordinatesDistance / transitionDistance;
	shadowCoords.w = clamp(1 - shadowCoordinatesDistance, 0, 1);

	float total = 0;
	float bias = 0.005;
	for (int x = -pcfCount; x <= pcfCount; x++) {
		for (int y = -pcfCount; y <= pcfCount; y++) {
			if (shadowCoords.z > texture2D(shadowMap, shadowCoords.xy + vec2(x, y) * texelSize).r + bias) {
				total += 1;
			}
		}
	}

	total /= totalTexels;
	float lightFactor = 1 - (total * shadowCoords.w);

	for (int i = 0; i < MAX_LIGHTS; i++) {
		if (dot(lightColour[i], lightColour[i]) == 0)
			continue;

		vec3 toLightVector = lightPosition[i] - viewPosition;
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

	totalDiffuse = max(totalDiffuse * lightFactor * texture2D(ssao, tc).x, minDiffuse);
	if (showLightmaps) {
		out_diffuse = vec4(totalDiffuse, 1);
	} else {
		out_diffuse = vec4(totalDiffuse, 1) * diffuse + vec4(totalSpecular, 1);

		float visibility = exp(-pow((distance * density), gradient));
		out_diffuse = mix(vec4(skyColour, 1), out_diffuse, clamp(visibility, 0, 1));
	}

	out_position = vec4(0);
	out_normal = vec4(0);
	out_bloom = vec4(0);
}

