#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec4 v_position;
out vec2 tc;

uniform mat4 shadowProjectionViewMatrix;
uniform mat4 transformationMatrix;

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	v_position = shadowProjectionViewMatrix * transformationMatrix * vec4(position, 1);
	gl_Position = v_position;
	tc = (textureCoordinates / numberOfRows) + offset;
}
