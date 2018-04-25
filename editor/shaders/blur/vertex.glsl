#version 150

in vec2 position;

out vec2 blur[11];

uniform vec2 unit;

void main(void) {
	gl_Position = vec4(position, 0, 1);
	vec2 tc = position * 0.5 + 0.5;

	for (int i = -5; i <= 5; i++)
		blur[i + 5] = tc + vec2(unit * i);
}
