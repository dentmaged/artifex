const float step = 0.1;
const float minRayStep = 0.1;
const float maxSteps = 30;
const int numBinarySearchSteps = 5;

vec3 binarySearch(inout vec3 dir, inout vec3 hitCoord, inout float dDepth) {
	float depth;
	vec4 projectedCoord;

	int i;
	for (i = 0; i < numBinarySearchSteps; i++) {
		projectedCoord = projectionMatrix * vec4(hitCoord, 1);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getPosition(projectedCoord.xy).z;
		dDepth = hitCoord.z - depth;
		dir *= 0.5;
		if (dDepth > 0)
			hitCoord += dir;
		else
			hitCoord -= dir;
	}

	projectedCoord = projectionMatrix * vec4(hitCoord, 1);
	projectedCoord.xy /= projectedCoord.w;
	projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

	return vec3(projectedCoord.xy, depth);
}

vec4 raymarch(vec3 dir, inout vec3 hitCoord, out float dDepth) {
	dir *= step;
	float depth;
	int steps;
	vec4 projectedCoord;

	for (int i = 0; i < maxSteps; i++) {
		hitCoord += dir;
		projectedCoord = projectionMatrix * vec4(hitCoord, 1);
		projectedCoord.xy /= projectedCoord.w;
		projectedCoord.xy = projectedCoord.xy * 0.5 + 0.5;

		depth = getPosition(projectedCoord.xy).z;
		if (depth > 1000)
			continue;

		dDepth = hitCoord.z - depth;
		if (dir.z - dDepth < 1.2) {
			if (dDepth <= 0) {
				return vec4(binarySearch(dir, hitCoord, dDepth), 1);
			}
		}

		steps++;
	}

	return vec4(projectedCoord.xy, depth, 0);
}

vec3 getReflection(vec3 hitPosition, vec3 reflected, float otherFactors) {
	float dDepth;
	vec4 coords = raymarch(reflected * max(minRayStep, -hitPosition.z), vec3(hitPosition), dDepth);

	vec2 dCoords = smoothstep(0.2, 0.6, abs(vec2(0.5) - coords.xy));
	float screenEdgeFactor = clamp(1 - (dCoords.x + dCoords.y), 0, 1);
	float factor = clamp(otherFactors * screenEdgeFactor * -reflected.z, 0, 1);
	reflected = normalize((mat3(inverseViewMatrix) * reflected).xyz);

	return mix(getSkyColour(reflected).diffuse.xyz, texture2D(scene, coords.xy).xyz, factor);
}

vec3 getReflection(vec3 hitPosition, vec3 reflected) {
	return getReflection(hitPosition, reflected, 1);
}
