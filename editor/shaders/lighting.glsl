#define LIGHTING_GLSL

tex shadowMaps[SHADOW_SPLITS];

uniform bool showLightmaps;
uniform bool diffuseOnly;

uniform mat4 toShadowMapSpaces[SHADOW_SPLITS];
uniform float shadowDistances[SHADOW_SPLITS];

uniform vec4 lightPosition[MAX_LIGHTS];
uniform vec3 lightColour[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightDirection[MAX_LIGHTS];
uniform vec2 lightCutoff[MAX_LIGHTS];

const float transitionDistance = 8;

#define PCF

#if defined(PCF)
	const int pcfCount = 4;
	const int totalTexels = (pcfCount * 2 + 1) * (pcfCount * 2 + 1);
	const vec2 texelSize = vec2(1.0 / 2048.0);
#elif defined(POISSON)
	const int poissonCount = 8;
	const vec2 poissonDisk[16] = vec2[](
		vec2(-0.94201624, -0.39906216),
		vec2(0.94558609, -0.76890725),
		vec2(-0.094184101, -0.92938870),
		vec2(0.34495938, 0.29387760),
		vec2(-0.91588581, 0.45771432),
		vec2(-0.81544232, -0.87912464),
		vec2(-0.38277543, 0.27676845),
		vec2(0.97484398, 0.75648379),
		vec2(0.44323325, -0.97511554),
		vec2(0.53742981, -0.47373420),
		vec2(-0.26496911, -0.41893023),
		vec2(0.79197514, 0.19090188),
		vec2(-0.24188840, 0.99706507),
		vec2(-0.81409955, 0.91437590),
		vec2(0.19984126, 0.78641367),
		vec2(0.14383161, -0.14100790)
	);
	const vec2 texelSize = vec2(0.5 / 2048.0);
#endif

vec3 fresnel(float cosTheta, vec3 F0) {
	return F0 + (1.0 - F0) * pow(1 - cosTheta, 5.0); // schlick
}

float distributionGGX(vec3 N, vec3 H, float roughness) { // normal distribution (cook-torrence)
	float a = roughness * roughness;
	float a2 = a * a;
	float NdotH = max(dot(N, H), 0.0);
	float NdotH2 = NdotH * NdotH;

	float num = a2;
	float denom = (NdotH2 * (a2 - 1) + 1.0);

	return num / (pi * denom * denom);
}

float geometryGGX(float NdotV, float roughness) { // cook-torrence
	float r = roughness + 1.0;
	float k = (r * r) / 8.0;

	float num = NdotV;
	float denom = NdotV * (1 - k) + k;

	return num / denom;
}

float geometrySmith(float NdotV, float NdotL, float roughness) {
	float ggx2 = geometryGGX(NdotV, roughness);
	float ggx1 = geometryGGX(NdotL, roughness);

	return ggx1 * ggx2;
}

float chebyshev(float distance, vec2 moments) {
	if (distance <= moments.x)
		return 1;

	float variance = max(moments.y - (moments.x * moments.x), 0.000075);
	float d = distance - moments.x;
	return smoothstep(0.2, 1.0, variance / (variance + d * d));
}

vec3 performLighting(vec3 viewPosition, vec3 normal, vec3 albedo, float metallic, float materialSpecular, float roughness) {
	if (diffuseOnly)
		return albedo;

	if (showLightmaps)
		albedo = vec3(1.0);

	float distance = length(viewPosition);
	vec3 V = normalize(-viewPosition);
	vec3 N = normalize(normal);
	vec3 R = reflect(-V, N);

	float NdotV = max(dot(N, V), 0.0);

	int map = 0;
	for (int i = 0; i < SHADOW_SPLITS; i++)
		if (shadowDistances[i] < distance)
			map = i;

	mat4 toShadowMapSpace = toShadowMapSpaces[map];
	float shadowDistance = shadowDistances[map] * 2.0;

	vec3 wPosition = (inverseViewMatrix * vec4(viewPosition, 1.0)).xyz;
	vec4 shadowCoords = toShadowMapSpace * vec4(viewPosition, 1.0);
	if (map == SHADOW_SPLITS - 1) {
		float shadowCoordinatesDistance = distance - (shadowDistance - transitionDistance);
		shadowCoordinatesDistance = shadowCoordinatesDistance / transitionDistance;
		shadowCoords.w = clamp(1.0 - shadowCoordinatesDistance, 0.0, 1.0);
	}

	vec3 F0 = vec3(0.08 * materialSpecular);
	F0 = mix(F0, albedo, metallic);

	vec3 Lo = vec3(0.0);
	for (int i = 0; i < MAX_LIGHTS; i++) {
		if (dot(lightColour[i], lightColour[i]) == 0)
			continue;

		float shadow = 1.0;

		vec3 L = normalize(lightPosition[i].xyz - viewPosition);
		if (lightPosition[i].w == 1)
			L = normalize(lightDirection[i].xyz);
		vec3 H = normalize(V + L);

		float distance = length(lightPosition[i].xyz - viewPosition);
		float attenuation = 1.0 / (attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance));
		if (lightPosition[i].w == 1.0)
			attenuation = 1;

		vec3 radiance = lightColour[i] * attenuation;
		float NdotL = max(dot(N, L), 0.0);

		float normalDistribution = distributionGGX(N, H, roughness);
		float geometry = geometrySmith(NdotV, NdotL, roughness);
		vec3 fresnel = fresnel(max(dot(H, V), 0.0), F0);
		vec3 specular = (normalDistribution * geometry * fresnel) / max(4 * NdotV * NdotL, 0.0001);

		vec3 kD = 1 - fresnel;
		kD *= 1 - metallic;

		if (lightPosition[i].w == 1 && shadow == 1) {
			float total = 0.0;
			if (map > 0) {
				float bias = 0.003;

#if defined(PCF)
				for (int x = -pcfCount; x <= pcfCount; x++)
					for (int y = -pcfCount; y <= pcfCount; y++)
						if (shadowCoords.z > texture2D(shadowMaps[map], shadowCoords.xy + vec2(x, y) * texelSize).r + bias)
#elif defined(POISSON)

				for (int i = 0; i < poissonCount; i++)
					if (shadowCoords.z > texture2D(shadowMaps[map], shadowCoords.xy + poissonDisk[int(16 * fract(sin(dot(vec4(floor(wPosition * 10000), i), vec4(12.9898, 78.233, 45.164, 94.673))) * 43758.5453)) % 16] * texelSize).r + bias)
#endif
							total += 1;

#if defined(PCF)
				total /= totalTexels;
#elif defined(POISSON)
				total /= poissonCount;
#endif
				shadow = 1.0 - (total * shadowCoords.w);
			} else {
				vec2 moments = texture2D(shadowMaps[map], shadowCoords.xy).xy;
				shadow = chebyshev(shadowCoords.z, moments);
			}
		}

		float intensity = 1.0;
		vec2 cutoff = lightCutoff[i];
		if (lightPosition[i].w == 2)
			intensity = clamp((dot(L, normalize(-lightDirection[i])) - cutoff.y) / (cutoff.x - cutoff.y), 0, 1);

		if (shadow > 0 && NdotL > 0 && intensity > 0)
			Lo += (kD * albedo.xyz / pi + specular) * radiance * NdotL * shadow * intensity;
	}

	return Lo;
}
