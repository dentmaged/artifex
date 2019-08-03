#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex other;
tex normal;
tex ssao;
tex depthMap;
tex albedo;

texCube irradianceMap[MAX_PROBES];
texCube prefilter[MAX_PROBES];
tex brdf;

uniform float irradianceScale;
uniform float ambientScale;
uniform float mips;

uniform vec2 prefilterPccSize[MAX_PROBES];
uniform vec3 prefilterPccPosition[MAX_PROBES];

uniform vec2 irradiancePccSize[MAX_PROBES];
uniform vec3 irradiancePccPosition[MAX_PROBES];

uniform mat4 projectionMatrix;
uniform mat4 inverseProjectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 inverseViewMatrix;

#define ssrDepthMap depthMap
#include "util.glsl"
#include "reflection.glsl"

vec3 fresnelRoughness(float cosTheta, vec3 F0, float roughness) {
	return F0 + (max(F0, 1.0 - roughness) - F0) * pow(1 - cosTheta, 5.0);
}

vec3 pcc(vec3 wDir, vec3 pccPosition, vec3 pccSize, vec3 wPosition, out float strength) {
	if (dot(pccSize, pccSize) > 0) {
		vec3 pccRelative = pccPosition - wPosition;

		vec3 maxIntersect = (pccRelative + pccSize * 0.5) / wDir;
		vec3 minIntersect = (pccRelative - pccSize * 0.5) / wDir;
		vec3 furtherIntersect = max(maxIntersect, minIntersect);

		float distance = min(min(furtherIntersect.x, furtherIntersect.y), furtherIntersect.z);
		strength = smoothstep(0.05, 0.3, length(wPosition - pccPosition) / length(pccSize));

		return wPosition + wDir * distance - pccPosition;
	}
	strength = 0;

	return wDir;
}

vec3 performLighting(vec3 viewPosition, vec3 albedo, vec3 normal, float metallic, float materialSpecular, float roughness, float ao) {
	float distance = length(viewPosition);
	if (distance > 1000 || (normal.x == 1 && normal.y == 1 && normal.z == 1)) // skybox
		discard;

	vec3 V = normalize(-viewPosition);
	vec3 N = normalize(normal);
	vec3 R = reflect(-V, N);

	float NdotV = max(dot(N, V), 0.0);
	vec3 wPosition = (inverseViewMatrix * vec4(viewPosition, 1)).xyz;

	vec3 F0 = vec3(0.08 * materialSpecular);
	F0 = mix(F0, albedo, metallic);

	vec3 fresnel = fresnelRoughness(max(NdotV, 0.0), F0, roughness);
	vec3 kD = 1 - fresnel;
	kD *= 1 - metallic;

	vec3 ambient = vec3(0.0);
	vec3 irradianceSampleDirection = mat3(inverseViewMatrix) * N;
	vec3 prefilterSampleDirection = mat3(inverseViewMatrix) * R;

	vec2 brdf = texture2D(brdf, vec2(NdotV, roughness)).xy;
	vec3 specularBRDF = fresnel * brdf.x + brdf.y;

	vec3 ssrData = getReflection(viewPosition.xyz, R);
	vec4 ssrColour = textureLod(scene, ssrData.xy, roughness * mips);

	bool skyReflection = true;
	float s;
	for (int i = MAX_PROBES - 1; i >= 0; i--) {
		if (prefilterPccSize[i].y == 0.0 && irradiancePccSize[i].y == 0.0)
			continue;

		float strength;
		vec3 irradiance = texture(irradianceMap[i], pcc(irradianceSampleDirection, irradiancePccPosition[i], vec3(irradiancePccSize[i].x), wPosition, strength)).xyz * irradianceScale * 1;
		vec3 diffuse = irradiance * albedo;

		vec3 prefiltered = textureLod(prefilter[i], pcc(prefilterSampleDirection, prefilterPccPosition[i], vec3(prefilterPccSize[i].x), wPosition, strength), roughness * 8.0).xyz;
		if (i > 0) {
			if (prefiltered.x > 0.95 && prefiltered.y < 0.05 && prefiltered.z < 0.05)
				prefiltered = vec3(0);
			else
				skyReflection = false;
		} else {
			if (!skyReflection)
				prefiltered = vec3(0);
			else
				prefiltered *= 3;
		}
		vec3 reflectedColour = mix(prefiltered * 1, ssrColour.xyz * ssrColour.w, ssrData.z);

		s = strength;

		ambient += kD * diffuse + reflectedColour * specularBRDF;
	}

	return ambientScale * ao * ambient;
}

void main(void) {
	vec3 albedo = texture2D(albedo, tc).xyz;
	vec4 other = texture2D(other, tc);
	vec4 normal = texture2D(normal, tc);

	float metallic = other.y;
	float materialSpecular = normal.w;
	float roughness = other.x;
	float ao = texture2D(ssao, tc).r * normal.w;

	out_colour = vec4(performLighting(getPosition(tc), albedo, normal.xyz, metallic, 0.5, roughness, ao), 1);
}
