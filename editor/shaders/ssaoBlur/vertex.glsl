#version 150

in vec2 position;

out vec2 blur[5];

uniform vec2 unit;

void main(void) {
	gl_Position = vec4(position, 0, 1);
	vec2 tc = position * 0.5 + 0.5;

	for (int i = -2; i <= 2; i++)
		blur[i + 2] = tc + vec2(unit * i);
}
