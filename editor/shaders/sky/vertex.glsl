#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec4 worldPosition;
out vec3 s_normal;
out vec3 pos;
out vec2 tc;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

const float PI = 3.14159265358979;

void main(void) {
	worldPosition = transformationMatrix * vec4(position, 1);
	vec4 positionRelativeToCamera = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCamera;

	pos = position;

	s_normal = (transformationMatrix * vec4(normal, 0)).xyz;
	tc = textureCoordinates;
}
