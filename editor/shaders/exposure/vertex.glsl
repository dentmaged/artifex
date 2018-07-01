#version 150

in vec2 position;

out vec2 tc;

void main(void) {
	gl_Position = vec4(position, 0, 1);
}
