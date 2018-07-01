#version 330

in vec3 pos;

FS_OUT(colour)

texCube environment;
uniform float mip;
uniform float face;

const float pi = 3.141592653589793238;
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

void main(void) {
	vec3 N = normalize(pos);
	vec3 R = N;
	vec3 V = R;

	vec3 prefilteredColor = vec3(0.0);
	float totalWeight = 0;
	float NdotLCount;

	for (uint i = 0u; i < sampleCount; i++) {
		vec2 Xi = hammersley(i, sampleCount);
		vec3 H = importanceSampleGGX(Xi, N, mip);
		vec3 L = normalize(2 * dot(V, H) * H - V);

		float NdotL = max(dot(N, L), 0);
		if (NdotL > 0) {
			float D = distributionGGX(N, H, mip);
			float NdotH = max(dot(N, H), 0);
			float HdotV = max(dot(H, V), 0);
			float pdf = D * NdotH / (4 * HdotV) + 0.0001;

			float resolution = 1024; // resolution of cubemap
			float saTexel = 4 * pi / (6 * resolution * resolution);
			float saSample = 1.0 / (float(sampleCount) * pdf + 0.0001);

			float mipLevel = mip == 0 ? 0 : 0.5 * log2(saSample / saTexel);
			prefilteredColor += textureLod(environment, L, mipLevel).rgb * NdotL;
			totalWeight += NdotL;
			NdotLCount++;
		}
	}

	out_colour = vec4(prefilteredColor / totalWeight, 1);
	// out_colour = vec4(face < 2 ? face : 0, face > 1 && face < 4 ? face - 2 : 0, face > 3 ? face - 4 : 0, 1);
}
