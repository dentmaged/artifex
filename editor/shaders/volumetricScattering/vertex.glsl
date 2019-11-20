#version 150

in vec2 position;

out vec2 tc;

void main(void) {
	gl_Position = vec4(position, 0.0, 1.0);
	tc = position * 0.5 + 0.5;
}
