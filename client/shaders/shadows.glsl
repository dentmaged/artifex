#define SHADOWS_GLSL

tex shadowMaps[SHADOW_SPLITS];

uniform mat4 toShadowMapSpaces[SHADOW_SPLITS];
uniform float shadowDistances[SHADOW_SPLITS];

#define PCF

#if defined(PCF)
	const int pcfCount = 3;
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
	const vec2 texelSize = vec2(1.0 / 2048.0);
#endif

const float transitionDistance = 8;

float performShadows(vec3 viewPosition) {
	float distance = length(viewPosition);

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

	float total = 0.0;
	float bias = 0.003;

#if defined(PCF)
	for (int x = -pcfCount; x <= pcfCount; x++)
		for (int y = -pcfCount; y <= pcfCount; y++)
			if (shadowCoords.z > texture2D(shadowMaps[map], shadowCoords.xy + vec2(x, y) * texelSize).x + bias)
#elif defined(POISSON)
	for (int i = 0; i < poissonCount; i++)
		if (shadowCoords.z > texture2D(shadowMaps[map], shadowCoords.xy + poissonDisk[int(16 * fract(sin(dot(vec4(floor(wPosition * 10000), i), vec4(12.9898, 78.233, 45.164, 94.673))) * 43758.5453)) % 16] * texelSize).x + bias)
#endif
				total += 1;

#if defined(PCF)
	total /= totalTexels;
#elif defined(POISSON)
	total /= poissonCount;
#endif
	return 1.0 - (total * shadowCoords.w);
}
