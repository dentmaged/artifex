#version 330

in vec4 viewPosition;
in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(position)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex modelTexture;
uniform float shineDamper;
uniform float reflectivity;
uniform vec4 colour;

void main(void) {
	out_diffuse = texture2D(modelTexture, tc);
	if (out_diffuse.a < 0.5)
		discard;

	out_diffuse.xyz = mix(pow(out_diffuse.xyz, vec3(GAMMA)), colour.xyz, colour.a);
	out_position = vec4(viewPosition);
	out_normal = vec4(s_normal, reflectivity);
	out_bloom = vec4(0, 0, 0, 1);
	out_godrays = vec4(0, 0, 0, 1);

	out_diffuse.a = shineDamper;
}
