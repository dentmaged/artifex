#version 330

in vec4 v_position;
in vec2 tc;

FS_OUT(colour)

tex albedo;

void main(void) {
	out_colour = texture2D(albedo, tc);
	if (out_colour.a < 0.5)
		discard;

	float depth = v_position.z / v_position.w * 0.5 + 0.5;

	vec2 d = vec2(dFdx(depth), dFdy(depth));
	float m2 = depth * depth + 0.25 * dot(d, d);

	out_colour = vec4(depth, m2, 0, 1);
}
