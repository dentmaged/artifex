#version 330

in vec4 viewPosition;
in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(position)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex blendmap;
tex backgroundTexture;
tex rTexture;
tex gTexture;
tex bTexture;
uniform vec4 colour;

uniform mat4 viewMatrix;

void main(void) {
	vec4 blendMapColour = texture2D(blendmap, tc);

	float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
	vec2 tiled = tc * 30;
	vec4 backgroundTextureColour = texture2D(backgroundTexture, tiled) * backTextureAmount;
	vec4 rTextureColour = texture2D(rTexture, tiled) * blendMapColour.r;
	vec4 gTextureColour = texture2D(gTexture, tiled) * blendMapColour.g;
	vec4 bTextureColour = texture2D(bTexture, tiled) * blendMapColour.b;

	out_diffuse = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;
	out_diffuse.xyz = mix(pow(out_diffuse.xyz, vec3(GAMMA)), colour.xyz, colour.a);
	out_position = vec4(viewPosition.xyz, 1);
	out_normal = vec4(s_normal, 0);
	out_bloom = vec4(0, 0, 0, 1);
	out_godrays = vec4(0, 0, 0, 1);

	out_diffuse.a = 1;
}
