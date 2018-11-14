#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 pos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main(void) {
	mat4 pvtm = projectionMatrix * viewMatrix * transformationMatrix;
	gl_Position = pvtm * vec4(position, 1);
	pos = position;
}
