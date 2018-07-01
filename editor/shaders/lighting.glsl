tex shadowMaps[SHADOW_SPLITS];
texCube irradianceMap;
texCube prefilter;
tex brdf;

uniform bool showLightmaps;
uniform bool diffuseOnly;

uniform float minDiffuse;
uniform float density;
uniform float gradient;
uniform vec3 skyColour;

uniform mat4 toShadowMapSpaces[SHADOW_SPLITS];
uniform float shadowDistances[SHADOW_SPLITS];

uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform vec3 lightColour[MAX_LIGHTS];

const float transitionDistance = 3;
const int pcfCount = 6;
const int totalTexels = (pcfCount * 2 + 1) * (pcfCount * 2 + 1);
const float texelSize = 1.0 / 2048.0;

vec3 fresnel(float cosTheta, vec3 F0) {
	return F0 + (1 - F0) * pow(1 - cosTheta, 5); // schlick
}

vec3 fresnelRoughness(float cosTheta, vec3 F0, float roughness) {
	return F0 + (max(F0, 1 - roughness) - F0) * pow(1 - cosTheta, 5);
}

float distributionGGX(vec3 N, vec3 H, float roughness) { // normal distribution (cook-torrence)
	float a = roughness * roughness;
	float a2 = a * a;
	float NdotH = max(dot(N, H), 0.0);
	float NdotH2 = NdotH * NdotH;

	float num = a2;
	float denom = (NdotH2 * (a2 - 1) + 1);

	return num / (pi * denom * denom);
}

float geometryGGX(float NdotV, float roughness) { // cook-torrence
	float r = roughness + 1;
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

vec3 performLighting(vec3 viewPosition, vec3 worldPosition, vec3 normal, vec3 albedo, float metallic, float materialSpecular, float roughness, float ao) {
	if (diffuseOnly)
		return albedo;

	if (showLightmaps)
		albedo = vec3(1);

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
	float shadowDistance = shadowDistances[map] * 2;

	vec4 shadowCoords = toShadowMapSpace * vec4(worldPosition, 1);
	if (map == SHADOW_SPLITS - 1) {
		float shadowCoordinatesDistance = distance - (shadowDistance - transitionDistance);
		shadowCoordinatesDistance = shadowCoordinatesDistance / transitionDistance;
		shadowCoords.w = clamp(1 - shadowCoordinatesDistance, 0, 1);
	}

	vec3 F0 = vec3(0.08 * materialSpecular);
	F0 = mix(F0, albedo.xyz, metallic);

	vec3 Lo = vec3(0);
	vec3 sLo = vec3(0);
	for (int i = 0; i < MAX_LIGHTS; i++) {
		if (dot(lightColour[i], lightColour[i]) == 0)
			continue;

		vec3 L = normalize(lightPosition[i] - viewPosition);
		vec3 H = normalize(V + L);

		float distance = length(lightPosition[i] - viewPosition);
		float attenuation = 1.0 / (attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance));
		vec3 radiance = lightColour[i] * attenuation;
		float NdotL = max(dot(N, L), 0);

		float normalDistribution = distributionGGX(N, H, roughness);
		float geometry = geometrySmith(NdotV, NdotL, roughness);
		vec3 fresnel = fresnel(max(dot(H, V), 0), F0);
		vec3 specular = (normalDistribution * geometry * fresnel) / max(4 * NdotV * NdotL, 0.0001);

		vec3 kD = 1 - fresnel;
		kD *= 1 - metallic;
		float shadow = 1;

		if (i == 0) {
			float total = 0;
			float bias = 0.002;

			for (int x = -pcfCount; x <= pcfCount; x++)
				for (int y = -pcfCount; y <= pcfCount; y++)
					if (shadowCoords.z > texture2D(shadowMaps[map], shadowCoords.xy + vec2(x, y) * texelSize).r + bias)
						total += 1;

			total /= totalTexels;
			shadow = 1 - (total * shadowCoords.w);
		}

		if (shadow == 0 || NdotL == 0)
			continue;

		Lo += (kD * albedo.xyz / pi + specular) * radiance * NdotL * shadow;
		sLo += kD / albedo.xyz * attenuation * NdotL * shadow;
	}

	vec3 fresnel = fresnelRoughness(max(NdotV, 0), F0, roughness);
	vec3 kD = 1 - fresnel;
	kD *= 1 - metallic;

	vec3 irradiance = texture(irradianceMap, normalize(mat3(inverseViewMatrix) * N)).xyz;
	vec3 diffuse = irradiance * albedo;

	vec3 prefiltered = textureLod(prefilter, mat3(inverseViewMatrix) * R, roughness * 4).xyz;
	vec2 brdf = texture2D(brdf, vec2(NdotV, roughness)).xy;
	vec3 specular = prefiltered * (fresnel * brdf.x + brdf.y);

	vec3 ambient = (kD * diffuse + specular) * ao;
	float visibility = exp(-pow((distance * density), gradient));

	return mix(skyColour, clamp(sLo + 0.05, 0, 1) * ambient + Lo, clamp(visibility, 0, 1));
}
