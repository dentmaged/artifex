#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out mat3 tbn;
out vec2 tc;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 normalMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	gl_Position = projectionViewTransformationMatrix * vec4(position, 1);

	vec3 normal = normalize(mat3(normalMatrix) * normal);
	vec3 tangent = normalize(mat3(normalMatrix) * tangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = normalize(cross(normal, tangent));
	tbn = mat3(tangent, bitangent, normal);

	tc = (textureCoordinates / numberOfRows) + offset;
}
