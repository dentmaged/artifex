#define LIGHTING_GLSL

uniform bool showLightmaps;
uniform bool diffuseOnly;

uniform vec4 lightPosition[MAX_LIGHTS];
uniform vec4 lightColour[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightDirection[MAX_LIGHTS];
uniform vec2 lightCutoff[MAX_LIGHTS];

vec3 fresnel(float cosTheta, vec3 F0) {
	return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0); // schlick
}

float distributionGGX(vec3 N, vec3 H, float roughness) { // normal distribution (cook-torrence)
	float a = roughness * roughness;
	float a2 = a * a;
	float NdotH = max(dot(N, H), 0.0);
	float NdotH2 = NdotH * NdotH;

	float num = a2;
	float denom = (NdotH2 * (a2 - 1.0) + 1.0);

	return num / (pi * denom * denom);
}

float geometryGGX(float NdotV, float roughness) { // cook-torrence
	float r = roughness + 1.0;
	float k = (r * r) * 0.125;

	float num = NdotV;
	float denom = NdotV * (1.0 - k) + k;

	return num / denom;
}

float geometrySmith(float NdotV, float NdotL, float roughness) {
	float ggx2 = geometryGGX(NdotV, roughness);
	float ggx1 = geometryGGX(NdotL, roughness);

	return ggx1 * ggx2;
}

#if defined(SHADOWS_GLSL)
vec3 performLighting(vec3 viewPosition, vec3 normal, vec3 albedo, float metallic, float materialSpecular, float roughness) {
	vec4 shadowInfo = vec4(performShadows(viewPosition), 1, 1, 1);
#else
vec3 performLighting(vec3 viewPosition, vec3 normal, vec3 albedo, float metallic, float materialSpecular, float roughness, vec4 shadowInfo) {
#endif
	if (diffuseOnly)
		return albedo;

	if (showLightmaps)
		albedo = vec3(1.0);

	float distance = length(viewPosition);
	if (distance > 3250.0)
		return albedo;

	vec3 V = normalize(-viewPosition);
	vec3 N = normalize(normal);
	vec3 R = reflect(-V, N);

	float NdotV = max(dot(N, V), 0.0);

	vec3 F0 = vec3(0.08 * materialSpecular);
	F0 = mix(F0, albedo, metallic);

	vec3 Lo = vec3(0.0);
	int shadowedCount = 0;
	for (int i = 0; i < MAX_LIGHTS; i++) {
		if (dot(lightColour[i].xyz, lightColour[i].xyz) == 0)
			continue;

		float lightType = lightPosition[i].w;
		float lightRoughness = max(roughness, lightColour[i].w); // lightColour[i].w = light min roughness
		bool castsShadows = false;
		if (lightType >= 3) {
			lightType -= 3;
			castsShadows = true;
		}

		float shadow = 1.0;
		vec3 L = normalize(lightPosition[i].xyz - viewPosition);
		if (lightType == 1)
			L = normalize(lightDirection[i].xyz);
		vec3 H = normalize(V + L);

		float distance = length(lightPosition[i].xyz - viewPosition);
		float attenuation = 1.0 / (attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance));
		if (lightType == 1.0)
			attenuation = 1.0;

		vec3 radiance = lightColour[i].xyz * attenuation;
		float NdotL = max(dot(N, L), 0.0);

		float normalDistribution = distributionGGX(N, H, lightRoughness);
		float geometry = geometrySmith(NdotV, NdotL, lightRoughness);
		vec3 fresnel = fresnel(max(dot(H, V), 0.0), F0);
		vec3 specular = (normalDistribution * geometry * fresnel) / max(4 * NdotV * NdotL, 0.0001);

		vec3 kD = 1.0 - fresnel;
		kD *= 1.0 - metallic;

		if (castsShadows) {
			if (shadowedCount == 0)
				shadow = shadowInfo.r;
			else if (shadowedCount == 1)
					shadow = shadowInfo.g;
			else if (shadowedCount == 2)
					shadow = shadowInfo.b;
			else if (shadowedCount == 3)
					shadow = shadowInfo.a;

			shadowedCount++;
		}

		float intensity = 1.0;
		vec2 cutoff = lightCutoff[i];
		if (lightType == 2)
			intensity = clamp((dot(L, normalize(-lightDirection[i])) - cutoff.y) / (cutoff.x - cutoff.y), 0.0, 1.0);

		if (shadow > 0 && NdotL > 0 && intensity > 0)
			Lo += (kD * albedo.xyz / pi + specular) * radiance * NdotL * shadow * intensity;
	}

	return Lo;
}
