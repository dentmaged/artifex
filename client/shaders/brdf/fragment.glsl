#version 330

in vec2 tc;

FS_OUT(colour)

const uint sampleCount = 1024u;

float distributionGGX(vec3 N, vec3 H, float roughness) {
	float a = roughness * roughness;
	float a2 = a * a;
	float NdotH = max(dot(N, H), 0);
	float NdotH2 = NdotH * NdotH;

	float nom = a2;
	float denom = (NdotH2 * (a2 - 1) + 1);

	return a2 / (pi * denom * denom);
}

float radicalInverseVdC(uint bits) {
	bits = (bits << 16u) | (bits >> 16u);
	bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
	bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
	bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
	bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);

	return float(bits) * 2.3283064365386963e-10;
}

vec2 hammersley(uint i, uint N) {
	return vec2(float(i) / float(N), radicalInverseVdC(i));
}

vec3 importanceSampleGGX(vec2 Xi, vec3 N, float roughness) {
	float a = roughness * roughness;

	float phi = 2.0 * pi * Xi.x;
	float cosTheta = sqrt((1 - Xi.y) / (1 + (a * a - 1) * Xi.y));
	float sinTheta = sqrt(1 - cosTheta * cosTheta);
	vec3 H = vec3(cos(phi) * sinTheta, sin(phi) * sinTheta, cosTheta);

	vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
	vec3 tangent = normalize(cross(up, N));
	vec3 bitangent = cross(N, tangent);

	return normalize(tangent * H.x + bitangent * H.y + N * H.z);
}

float geometryGGX(float NdotV, float roughness) { // cook-torrence
	float r = roughness;
	float k = (r * r) / 2.0;

	float num = NdotV;
	float denom = NdotV * (1 - k) + k;

	return num / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
	float NdotV = max(dot(N, V), 0);
	float NdotL = max(dot(N, L), 0);

	float ggx2 = geometryGGX(NdotV, roughness);
	float ggx1 = geometryGGX(NdotL, roughness);

	return ggx1 * ggx2;
}

vec2 integrateBRDF(float NdotV, float roughness) {
	vec3 V = vec3(sqrt(1.0 - NdotV * NdotV), 0, NdotV);

	float A = 0;
	float B = 0;

	vec3 N = vec3(0, 0, 1);
	for (uint i = 0u; i < sampleCount; i++) {
		vec2 Xi = hammersley(i, sampleCount);
		vec3 H = importanceSampleGGX(Xi, N, roughness);
		vec3 L = normalize(2.0 * dot(V, H) * H - V);

		float NdotL = max(L.z, 0);
		float NdotH = max(H.z, 0);
		float VdotH = max(dot(V, H), 0);

		if (NdotL > 0) {
			float G = geometrySmith(N, V, L, roughness);
			float G_Vis = (G * VdotH) / (NdotH * NdotV);
			float Fc = pow(1 - VdotH, 5.0);

			A += (1 - Fc) * G_Vis;
			B += Fc * G_Vis;
		}
	}

	A /= float(sampleCount);
	B /= float(sampleCount);

	return vec2(A, B);
}

void main(void) {
	out_colour = vec4(integrateBRDF(tc.x, tc.y), 0, 1);
}
