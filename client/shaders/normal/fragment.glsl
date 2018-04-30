#version 330

in vec4 viewPosition;
in mat3 tbn;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(position)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex modelTexture;
tex normalMap;
tex specularMap;
uniform float shineDamper;
uniform float reflectivity;
uniform vec4 colour;

void main(void) {
	out_diffuse = texture2D(modelTexture, tc);
	if (out_diffuse.a < 0.5)
		discard;

	out_diffuse.xyz = mix(pow(out_diffuse.xyz, vec3(GAMMA)), colour.xyz, colour.a);
	out_position = vec4(viewPosition);

	vec3 mappedNormal = texture2D(normalMap, tc).xyz * 2 - 1;
	out_normal = vec4(normalize(tbn * mappedNormal), reflectivity);

	out_bloom = vec4(0, 0, 0, 1);
	out_godrays = vec4(0, 0, 0, 1);

	out_diffuse.a = shineDamper;
}
