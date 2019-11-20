#define DISABLE_SSR // unhappy with the quality

const float step = 0.5;
const float minRayStep = 0.5;
const int maxSteps = 30;
const int numBinarySearchSteps = 5;

float getDepth(vec2 coords) {
	return getPosition(ssrDepthMap, coords).z;
}

vec3 binarySearch(inout vec3 dir, inout vec3 hitCoord, inout float dDepth) {
	float depth;
	vec4 projectedCoord;

	for (int i = 0; i < numBinarySearchSteps; i++) {
		projectedCoord = projectionMatrix * vec4(hitCoord, 1.0);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getDepth(projectedCoord.xy);
		dDepth = hitCoord.z - depth;
		dir *= 0.5;
		if (dDepth > 0)
			hitCoord += dir;
		else
			hitCoord -= dir;
	}

	projectedCoord = projectionMatrix * vec4(hitCoord, 1.0);
	projectedCoord.xy /= projectedCoord.w;
	projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

	return vec3(projectedCoord.xy, depth);
}

vec4 raymarch(vec3 dir, vec3 hitCoord, out float dDepth) {
	dir *= step;
	float depth;
	int steps;
	vec4 projectedCoord;

	for (int i = 0; i < maxSteps; i++) {
		hitCoord += dir;
		projectedCoord = projectionMatrix * vec4(hitCoord, 1.0);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getDepth(projectedCoord.xy);
		if (depth > 3250)
			continue;

		dDepth = hitCoord.z - depth;
		if (dir.z - dDepth < 6.0)
			if (dDepth <= 0.0)
				return vec4(binarySearch(dir, hitCoord, dDepth), 1.0);

		steps++;
	}

	return vec4(0.0);
}

vec3 getReflection(vec3 hitPosition, vec3 reflected, float otherFactors) {
#ifdef DISABLE_SSR
	return vec3(0);
#else
	float dDepth;
	vec4 coords = raymarch(reflected * max(minRayStep, -hitPosition.z), vec3(hitPosition), dDepth);
	vec2 dCoords = smoothstep(0.2, 0.6, abs(vec2(0.5, 0.5) - coords.xy));
	float screenEdge = clamp(1.0 - (dCoords.x + dCoords.y), 0.0, 1.0);

	return vec3(coords.xy, clamp(otherFactors * screenEdge * -reflected.z, 0.0, 1.0));
#endif
}

vec3 getReflection(vec3 hitPosition, vec3 reflected) {
	return getReflection(hitPosition, reflected, 1.0);
}
