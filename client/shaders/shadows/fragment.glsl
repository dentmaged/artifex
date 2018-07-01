#version 330

in vec2 tc;

FS_OUT(colour)

tex albedo;

void main(void) {
	vec4 out_colour = texture2D(albedo, tc);
	if (out_colour.a < 0.5)
		discard;
}
