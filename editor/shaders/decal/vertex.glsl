#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 s_normal;
out vec2 tc;
out vec4 clipSpace;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 normalMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	clipSpace = projectionViewTransformationMatrix * vec4(position, 1);
	gl_Position = clipSpace;

	s_normal = normalize(mat3(normalMatrix) * normal);
	tc = (textureCoordinates / numberOfRows) + offset;
}
