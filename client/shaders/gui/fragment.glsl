#version 330

in vec2 tc;

out vec4 out_colour;

uniform sampler2D a;

uniform float mip;

void main(void) {
	out_colour = texture2D(a, tc, mip);
}
