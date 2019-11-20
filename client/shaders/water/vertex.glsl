#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out vec3 viewPosition;
out vec4 clipSpace;
out vec2 tc;

uniform mat4 projectionMatrix;
uniform mat4 viewTransformationMatrix;

uniform vec2 uvScale;

vec2 f(vec2 a) {
	return a - floor(a);
}

void main(void) {
	vec4 view = viewTransformationMatrix * vec4(position, 1);
	viewPosition = view.xyz;

	clipSpace = projectionMatrix * view;
	gl_Position = clipSpace;

	tc = textureCoordinates * uvScale;
}
