#version 150

in vec2 position;

out vec2 tc;

uniform mat4 transformationMatrix;
uniform vec4 uv;

void main(void) {
	gl_Position = transformationMatrix * vec4(position, 0, 1);
	tc = position * 0.5 + 0.5;
	tc = vec2(uv.xy + tc * uv.zw);
}
