#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out vec4 viewPosition;
out mat3 tbn;
out vec2 tc;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;
uniform mat4 normalMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	vec4 worldPosition = transformationMatrix * vec4(position, 1);
	viewPosition = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * viewPosition;

	vec3 normal = normalize(mat3(normalMatrix) * normal);
	vec3 tangent = normalize(mat3(normalMatrix) * tangent);
	vec3 bitangent = normalize(mat3(normalMatrix) * cross(normal, tangent));
	tbn = mat3(tangent, bitangent, normal);

	tc = (textureCoordinates / numberOfRows) + offset;
}
