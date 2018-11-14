#version 330

in vec4 viewPosition;
in vec3 s_normal;
in vec2 tc;
in vec2 coords;
in float discardFragment;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex blendmap;
tex backgroundTexture;
tex rTexture;
tex gTexture;
tex bTexture;

uniform float size;
uniform vec4 colour;

void main(void) {
	if (discardFragment > 0)
		discard;

	vec4 blendMapColour = texture2D(blendmap, coords);

	float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
	vec2 tiled = tc * 0.3 * size;
	vec4 backgroundTextureColour = texture2D(backgroundTexture, tiled) * backTextureAmount;
	vec4 rTextureColour = texture2D(rTexture, tiled) * blendMapColour.r;
	vec4 gTextureColour = texture2D(gTexture, tiled) * blendMapColour.g;
	vec4 bTextureColour = texture2D(bTexture, tiled) * blendMapColour.b;

	out_diffuse = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;
	out_diffuse.xyz = mix(pow(out_diffuse.xyz, vec3(GAMMA)), colour.xyz, colour.a);
	out_other = vec4(1, 0, 0, 0);
	out_normal = vec4(s_normal.xy, 0, 0.5);
	out_albedo = out_diffuse;

	out_diffuse.a = 1;
}
