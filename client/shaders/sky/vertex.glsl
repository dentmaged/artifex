#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 pos;
out vec2 tc;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main(void) {
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1);
	pos = position;
	tc = textureCoordinates;
}
