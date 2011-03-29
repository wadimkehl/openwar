

uniform sampler2D m_Key0;
uniform sampler2D m_Key1;
uniform sampler2D m_Tex0;
uniform sampler2D m_Tex1;
uniform sampler2D m_Tex2;
uniform sampler2D m_Tex3;
uniform sampler2D m_Tex4;
uniform sampler2D m_Tex5;
uniform sampler2D m_Tex6;
uniform sampler2D m_Tex7;
uniform sampler2D m_Tex8;
uniform sampler2D m_Tex9;
uniform sampler2D m_Tex10;
uniform sampler2D m_Tex11;
uniform sampler2D m_Tex12;
uniform sampler2D m_Tex13;

varying vec4 DiffuseSum;

varying vec3 vNormal;
varying vec2 texCoord;
varying vec3 vPosition;
varying vec4 vLightDir;


vec4 computeDiffuse()
{
  
	vec4 key0=texture2D(m_Key0,texCoord);
	vec4 key1=texture2D(m_Key1,texCoord);


	vec4 t0=texture2D(m_Tex0,texCoord*64.0);
	vec4 t1=texture2D(m_Tex1,texCoord*64.0);
	vec4 t2=texture2D(m_Tex2,texCoord*64.0);
	vec4 t3=texture2D(m_Tex3,texCoord*64.0);
	vec4 t4=texture2D(m_Tex4,texCoord*64.0);
	vec4 t5=texture2D(m_Tex5,texCoord*128.0);
	vec4 t6=texture2D(m_Tex6,texCoord*64.0);
	vec4 t7=texture2D(m_Tex7,texCoord*64.0);
	vec4 t8=texture2D(m_Tex8,texCoord*64.0);
	vec4 t9=texture2D(m_Tex9,texCoord*64.0);
	vec4 t10=texture2D(m_Tex10,texCoord*32.0);
	vec4 t11=texture2D(m_Tex11,texCoord*64.0);


	vec4 outColor=t0*(key0.r * key1.r);
	outColor+=t1*(key0.g * key1.r);
	outColor+=t2*(key0.b * key1.r);
	outColor+=t3*(key0.a * key1.r);

	outColor+=t4*(key0.r * key1.g);
	outColor+=t5*(key0.g * key1.g);
	outColor+=t6*(key0.b * key1.g);
	outColor+=t7*(key0.a * key1.g);

	outColor+=t8*(key0.r * key1.b);
	outColor+=t9*(key0.g * key1.b);
	outColor+=t10*(key0.b * key1.b);
	outColor+=t11*(key0.a * key1.b);

        // overlay
	outColor.g+=(key1.a);

#ifdef USE_GRID
// somewhen in the future this grid will be used for green/red/brown path coloring
	vec4 t13=texture2D(m_Tex13,texCoord*256.0);
        outColor *=t13;
#endif

        return outColor;

}



void main(void)
{

    vec4 outColor = computeDiffuse();

    float diffuseFactor = max(0.0, dot(vNormal,normalize(vLightDir.xyz)));
    outColor = outColor*DiffuseSum * diffuseFactor;

    gl_FragColor = outColor;

}

