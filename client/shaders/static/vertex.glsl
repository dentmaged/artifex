#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out vec3 s_normal;
out vec2 tc;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 normalMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	gl_Position = projectionViewTransformationMatrix * vec4(position, 1);

	s_normal = normalize(mat3(normalMatrix) * normal);
	tc = (textureCoordinates / numberOfRows) + offset;
}