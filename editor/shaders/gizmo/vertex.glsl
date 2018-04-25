#version 330

in vec3 position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

void main(void) {
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position, 1);
}
