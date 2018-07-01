const float pi = 3.141592653589793238;

vec3 getPosition(vec2 coords) {
	vec3 raw = vec3(coords, texture2D(depthMap, coords).r);
	vec4 ssp = vec4(raw * 2 - 1, 1);
	vec4 view = inverseProjectionMatrix * ssp;

	return view.xyz / view.w;
}
