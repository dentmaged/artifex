vec3 getPosition(in sampler2D map, vec2 coords) {
	vec3 raw = vec3(coords, texture2D(map, coords).r);
	vec4 ssp = vec4(raw * 2.0 - 1.0, 1.0);
	vec4 view = inverseProjectionMatrix * ssp;

	return view.xyz / view.w;
}

vec3 getPosition(vec2 coords) {
	return getPosition(depthMap, coords);
}

vec3 getNormal(vec2 xy) {
	return vec3(xy, sqrt(1.0 - dot(xy, xy))); // assumes xy is normalised
}
