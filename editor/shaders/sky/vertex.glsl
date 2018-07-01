#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 pos;

uniform mat4 projectionViewTransformationMatrix;

void main(void) {
	gl_Position = projectionViewTransformationMatrix * vec4(position, 1);
	pos = position;
}
