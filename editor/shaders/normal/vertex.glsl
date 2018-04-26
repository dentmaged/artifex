#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out vec4 viewPosition;
out vec3 s_normal;
out vec3 s_tangent;
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

	s_normal = normalize((normalMatrix * vec4(normal, 0)).xyz);
	s_tangent = normalize((normalMatrix * vec4(tangent, 0)).xyz);
	s_tangent = normalize(s_tangent - dot(s_tangent, s_normal) * s_normal);
	tc = (textureCoordinates / numberOfRows) + offset;
}