#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 s_normal;
out vec2 tc;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 normalMatrix;

void main(void) {
	gl_Position = projectionViewTransformationMatrix * vec4(position, 1);

	s_normal = normalize((normalMatrix * vec4(normal, 0)).xyz);
	tc = textureCoordinates;
}
