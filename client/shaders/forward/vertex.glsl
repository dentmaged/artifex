#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 viewPosition;
out vec3 s_normal;
out vec2 tc;

uniform mat4 projectionMatrix;
uniform mat4 viewTransformationMatrix;
uniform mat4 normalMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	viewPosition = (viewTransformationMatrix * vec4(position, 1)).xyz;
	gl_Position = projectionMatrix * vec4(viewPosition, 1);

	s_normal = normalize(mat3(normalMatrix) * normal);
	tc = (textureCoordinates / numberOfRows) + offset;
}
