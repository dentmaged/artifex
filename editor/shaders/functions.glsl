float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 hash(vec3 a) {
	a = fract(a.xyz * 0.8);
	a += dot(a, a.yxz + 19.9);

	return fract((a.xxy + a.yxx) * a.zyx);
}

float hash(float n) {
	return fract(sin(n) * 758.5453123);
}
