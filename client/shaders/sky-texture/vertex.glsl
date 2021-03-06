#version 330

in vec3 position;
in vec2 textureCoordinates;

out vec3 pos;
out vec2 tc;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 inverseTransformationMatrix;

void main(void) {
	vec4 screen = projectionViewTransformationMatrix * vec4(position, 1);
	gl_Position = screen;

	pos = (inverseTransformationMatrix * inverseViewMatrix * inverseProjectionMatrix * screen).xyz;
}
