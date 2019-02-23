#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex bloom;
tex exposure;

void main(void) {
	out_colour = texture2D(scene, tc);
	out_colour *= texture2D(bloom, tc).z + length(max((out_colour.xyz - vec3(2.1 - texture2D(exposure, vec2(0.5)).x)) / out_colour.xyz, 0));
	out_colour = out_colour * (out_colour.x * 0.2126) + (out_colour.y * 0.7152) + (out_colour.z * 0.0722);
}
