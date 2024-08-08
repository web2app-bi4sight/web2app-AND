package com.huntmobi.web2app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HM_WebView {
    private Context mContext;
    private WebView mWebView;
    private final Handler mHandler;
    private String mParameter;
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";
    private static FingerprintCallback fingerprintCallback; // 回调接口

    public HM_WebView(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        mWebView = new WebView(mContext);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // 添加JavaScript接口，以便从WebView中调用Java代码
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidInterface");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // HTML加载成功后，自动发送JSON字符串
                sendJSONString();
            }
        });

        try {
//            mWebView.loadUrl("file:///android_asset/HM_Fingerprint.html");
            String htmlStr = "<!DOCTYPE html><html lang=\"en\"><head><meta http-equiv=\"content-type\" content=\"text/html\" charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>获取web指纹</title></head><script type=\"text/javascript\">!function(e,t,a){\"use strict\";\"undefined\"!=typeof window&&\"function\"==typeof define&&define.amd?define(a):\"undefined\"!=typeof module&&module.exports?module.exports=a():t.exports?t.exports=a():t.Fingerprint=a()}(0,this,function(){\"use strict\";var e=function(e,t){e=[e[0]>>>16,65535&e[0],e[1]>>>16,65535&e[1]],t=[t[0]>>>16,65535&t[0],t[1]>>>16,65535&t[1]];var a=[0,0,0,0];return a[3]+=e[3]*t[3],a[2]+=a[3]>>>16,a[3]&=65535,a[2]+=e[2]*t[3],a[1]+=a[2]>>>16,a[2]&=65535,a[2]+=e[3]*t[2],a[1]+=a[2]>>>16,a[2]&=65535,a[1]+=e[1]*t[3],a[0]+=a[1]>>>16,a[1]&=65535,a[1]+=e[2]*t[2],a[0]+=a[1]>>>16,a[1]&=65535,a[1]+=e[3]*t[1],a[0]+=a[1]>>>16,a[1]&=65535,a[0]+=e[0]*t[3]+e[1]*t[2]+e[2]*t[1]+e[3]*t[0],a[0]&=65535,[a[0]<<16|a[1],a[2]<<16|a[3]]},t=function(e,t){return 32===(t%=64)?[e[1],e[0]]:t<32?[e[0]<<t|e[1]>>>32-t,e[1]<<t|e[0]>>>32-t]:(t-=32,[e[1]<<t|e[0]>>>32-t,e[0]<<t|e[1]>>>32-t])},a=function(e,t){return[e[0]^t[0],e[1]^t[1]]},r=function(e,t){return 0===(t%=64)?e:t<32?[e[0]<<t|e[1]>>>32-t,e[1]<<t]:[e[1]<<t-32,0]},n=function(t){return t=a(t,[0,t[0]>>>1]),t=e(t,[4283543511,3981806797]),t=a(t,[0,t[0]>>>1]),t=e(t,[3301882366,444984403]),t=a(t,[0,t[0]>>>1])},i=function(e,t){e=[e[0]>>>16,65535&e[0],e[1]>>>16,65535&e[1]],t=[t[0]>>>16,65535&t[0],t[1]>>>16,65535&t[1]];var a=[0,0,0,0];return a[3]+=e[3]+t[3],a[2]+=a[3]>>>16,a[3]&=65535,a[2]+=e[2]+t[2],a[1]+=a[2]>>>16,a[2]&=65535,a[1]+=e[1]+t[1],a[0]+=a[1]>>>16,a[1]&=65535,a[0]+=e[0]+t[0],a[0]&=65535,[a[0]<<16|a[1],a[2]<<16|a[3]]},o=function(e){var t=[window.screen.width,window.screen.height];return e.screen.detectScreenOrientation&&t.sort().reverse(),t},l=function(){return\"Microsoft Internet Explorer\"===navigator.appName||!(\"Netscape\"!==navigator.appName||!/Trident/.test(navigator.userAgent))},s=function(e){if(null==navigator.plugins)return e.NOT_AVAILABLE;for(var t=[],a=0,r=navigator.plugins.length;a<r;a++)navigator.plugins[a]&&t.push(navigator.plugins[a]);return c(e)&&(t=t.sort(function(e,t){return e.name>t.name?1:e.name<t.name?-1:0})),u(t,function(e){var t=u(e,function(e){return[e.type,e.suffixes]});return[e.name,e.description,t]})},c=function(e){for(var t=!1,a=0,r=e.plugins.sortPluginsFor.length;a<r;a++){var n=e.plugins.sortPluginsFor[a];if(navigator.userAgent.match(n)){t=!0;break}}return t},u=function(e,t){var a=[];return null==e?a:Array.prototype.map&&e.map===Array.prototype.map?e.map(t):(d(e,function(e,r,n){a.push(t(e,r,n))}),a)},d=function(e,t){if(Array.prototype.forEach&&e.forEach===Array.prototype.forEach)e.forEach(t);else if(e.length===+e.length)for(var a=0,r=e.length;a<r;a++)t(e[a],a,e);else for(var n in e)e.hasOwnProperty(n)&&t(e[n],n,e)},g=function(e){var t=[],a=document.createElement(\"canvas\");a.width=2e3,a.height=200,a.style.display=\"inline\";var r=a.getContext(\"2d\");return r.rect(0,0,10,10),r.rect(2,2,6,6),t.push(\"canvas winding:\"+(!1===r.isPointInPath(5,5,\"evenodd\")?\"yes\":\"no\")),r.textBaseline=\"alphabetic\",r.fillStyle=\"#f60\",r.fillRect(125,1,62,20),r.fillStyle=\"#069\",e.dontUseFakeFontInCanvas?r.font=\"11pt Arial\":r.font=\"11pt no-real-font-123\",r.fillText(\"Cwm fjordbank glyphs vext quiz, 😃\",2,15),r.fillStyle=\"rgba(102, 204, 0, 0.2)\",r.font=\"18pt Arial\",r.fillText(\"Cwm fjordbank glyphs vext quiz, 😃\",4,45),r.globalCompositeOperation=\"multiply\",r.fillStyle=\"rgb(255,0,255)\",r.beginPath(),r.arc(50,50,50,0,2*Math.PI,!0),r.closePath(),r.fill(),r.fillStyle=\"rgb(0,255,255)\",r.beginPath(),r.arc(100,50,50,0,2*Math.PI,!0),r.closePath(),r.fill(),r.fillStyle=\"rgb(255,255,0)\",r.beginPath(),r.arc(75,100,50,0,2*Math.PI,!0),r.closePath(),r.fill(),r.fillStyle=\"rgb(255,0,255)\",r.arc(75,75,75,0,2*Math.PI,!0),r.arc(75,75,25,0,2*Math.PI,!0),r.fill(\"evenodd\"),a.toDataURL&&t.push(\"canvas fp:\"+a.toDataURL()),t},h=function(){var e,t=function(t){return e.clearColor(0,0,0,1),e.enable(e.DEPTH_TEST),e.depthFunc(e.LEQUAL),e.clear(e.COLOR_BUFFER_BIT|e.DEPTH_BUFFER_BIT),\"[\"+t[0]+\", \"+t[1]+\"]\"};if(!(e=m()))return null;var a=[],r=e.createBuffer();e.bindBuffer(e.ARRAY_BUFFER,r);var n=new Float32Array([-.2,-.9,0,.4,-.26,0,0,.732134444,0]);e.bufferData(e.ARRAY_BUFFER,n,e.STATIC_DRAW),r.itemSize=3,r.numItems=3;var i=e.createProgram(),o=e.createShader(e.VERTEX_SHADER);e.shaderSource(o,\"attribute vec2 attrVertex;varying vec2 varyinTexCoordinate;uniform vec2 uniformOffset;void main(){varyinTexCoordinate=attrVertex+uniformOffset;gl_Position=vec4(attrVertex,0,1);}\"),e.compileShader(o);var l=e.createShader(e.FRAGMENT_SHADER);e.shaderSource(l,\"precision mediump float;varying vec2 varyinTexCoordinate;void main() {gl_FragColor=vec4(varyinTexCoordinate,0,1);}\"),e.compileShader(l),e.attachShader(i,o),e.attachShader(i,l),e.linkProgram(i),e.useProgram(i),i.vertexPosAttrib=e.getAttribLocation(i,\"attrVertex\"),i.offsetUniform=e.getUniformLocation(i,\"uniformOffset\"),e.enableVertexAttribArray(i.vertexPosArray),e.vertexAttribPointer(i.vertexPosAttrib,r.itemSize,e.FLOAT,!1,0,0),e.uniform2f(i.offsetUniform,1,1),e.drawArrays(e.TRIANGLE_STRIP,0,r.numItems);try{a.push(e.canvas.toDataURL())}catch(e){}a.push(\"extensions:\"+(e.getSupportedExtensions()||[]).join(\";\")),a.push(\"webgl aliased line width range:\"+t(e.getParameter(e.ALIASED_LINE_WIDTH_RANGE))),a.push(\"webgl aliased point size range:\"+t(e.getParameter(e.ALIASED_POINT_SIZE_RANGE))),a.push(\"webgl alpha bits:\"+e.getParameter(e.ALPHA_BITS)),a.push(\"webgl antialiasing:\"+(e.getContextAttributes().antialias?\"yes\":\"no\")),a.push(\"webgl blue bits:\"+e.getParameter(e.BLUE_BITS)),a.push(\"webgl depth bits:\"+e.getParameter(e.DEPTH_BITS)),a.push(\"webgl green bits:\"+e.getParameter(e.GREEN_BITS)),a.push(\"webgl max anisotropy:\"+function(e){var t=e.getExtension(\"EXT_texture_filter_anisotropic\")||e.getExtension(\"WEBKIT_EXT_texture_filter_anisotropic\")||e.getExtension(\"MOZ_EXT_texture_filter_anisotropic\");if(t){var a=e.getParameter(t.MAX_TEXTURE_MAX_ANISOTROPY_EXT);return 0===a&&(a=2),a}return null}(e)),a.push(\"webgl max combined texture image units:\"+e.getParameter(e.MAX_COMBINED_TEXTURE_IMAGE_UNITS)),a.push(\"webgl max cube map texture size:\"+e.getParameter(e.MAX_CUBE_MAP_TEXTURE_SIZE)),a.push(\"webgl max fragment uniform vectors:\"+e.getParameter(e.MAX_FRAGMENT_UNIFORM_VECTORS)),a.push(\"webgl max render buffer size:\"+e.getParameter(e.MAX_RENDERBUFFER_SIZE)),a.push(\"webgl max texture image units:\"+e.getParameter(e.MAX_TEXTURE_IMAGE_UNITS)),a.push(\"webgl max texture size:\"+e.getParameter(e.MAX_TEXTURE_SIZE)),a.push(\"webgl max varying vectors:\"+e.getParameter(e.MAX_VARYING_VECTORS)),a.push(\"webgl max vertex attribs:\"+e.getParameter(e.MAX_VERTEX_ATTRIBS)),a.push(\"webgl max vertex texture image units:\"+e.getParameter(e.MAX_VERTEX_TEXTURE_IMAGE_UNITS)),a.push(\"webgl max vertex uniform vectors:\"+e.getParameter(e.MAX_VERTEX_UNIFORM_VECTORS)),a.push(\"webgl max viewport dims:\"+t(e.getParameter(e.MAX_VIEWPORT_DIMS))),a.push(\"webgl red bits:\"+e.getParameter(e.RED_BITS)),a.push(\"webgl renderer:\"+e.getParameter(e.RENDERER)),a.push(\"webgl shading language version:\"+e.getParameter(e.SHADING_LANGUAGE_VERSION)),a.push(\"webgl stencil bits:\"+e.getParameter(e.STENCIL_BITS)),a.push(\"webgl vendor:\"+e.getParameter(e.VENDOR)),a.push(\"webgl version:\"+e.getParameter(e.VERSION));try{var s=e.getExtension(\"WEBGL_debug_renderer_info\");s&&(a.push(\"webgl unmasked vendor:\"+e.getParameter(s.UNMASKED_VENDOR_WEBGL)),a.push(\"webgl unmasked renderer:\"+e.getParameter(s.UNMASKED_RENDERER_WEBGL)))}catch(e){}return e.getShaderPrecisionFormat?(d([\"FLOAT\",\"INT\"],function(t){d([\"VERTEX\",\"FRAGMENT\"],function(r){d([\"HIGH\",\"MEDIUM\",\"LOW\"],function(n){d([\"precision\",\"rangeMin\",\"rangeMax\"],function(i){var o=e.getShaderPrecisionFormat(e[r+\"_SHADER\"],e[n+\"_\"+t])[i];\"precision\"!==i&&(i=\"precision \"+i);var l=[\"webgl \",r.toLowerCase(),\" shader \",n.toLowerCase(),\" \",t.toLowerCase(),\" \",i,\":\",o].join(\"\");a.push(l)})})})}),a):a},T=function(){if(!f())return!1;var e=m();return!!window.WebGLRenderingContext&&!!e},m=function(){var e=document.createElement(\"canvas\"),t=null;try{t=e.getContext(\"webgl\")||e.getContext(\"experimental-webgl\")}catch(e){}return t||(t=null),t},f=function(){var e=document.createElement(\"canvas\");return!(!e.getContext||!e.getContext(\"2d\"))},p=[{key:\"ca\",getData:function(e,t){f()?e(g(t)):e(t.NOT_AVAILABLE)}},{key:\"wg\",getData:function(e,t){T()?e(h()):e(t.NOT_AVAILABLE)}},{key:\"pi\",getData:function(e,t){l()?t.plugins.excludeIE?e(t.EXCLUDED):e(getIEPlugins(t)):e(s(t))}},{key:\"ao\",getData:function(e,t){var a=t.audio;if(a.excludeIOS11&&navigator.userAgent.match(/OS 11.+Version\\/11.+Safari/))return e(t.EXCLUDED);var r=window.OfflineAudioContext||window.webkitOfflineAudioContext;if(null==r)return e(t.NOT_AVAILABLE);var n=new r(1,44100,44100),i=n.createOscillator();i.type=\"triangle\",i.frequency.setValueAtTime(1e4,n.currentTime);var o=n.createDynamicsCompressor();d([[\"threshold\",-50],[\"knee\",40],[\"ratio\",12],[\"reduction\",-20],[\"attack\",0],[\"release\",.25]],function(e){void 0!==o[e[0]]&&\"function\"==typeof o[e[0]].setValueAtTime&&o[e[0]].setValueAtTime(e[1],n.currentTime)}),i.connect(o),o.connect(n.destination),i.start(0),n.startRendering();var l=setTimeout(function(){return console.warn(\"Audio fingerprint timed out.\"+navigator.userAgent+'\".'),n.oncomplete=function(){},n=null,e(\"audioTimeout\")},a.timeout);n.oncomplete=function(t){var a;try{clearTimeout(l),a=t.renderedBuffer.getChannelData(0).slice(4500,5e3).reduce(function(e,t){return e+Math.abs(t)},0).toString(),i.disconnect(),o.disconnect()}catch(t){return void e(t)}e(a)}}},{key:\"se\",getData:function(e,t){e(o(t))}},{key:\"ft\",getData:function(e,t){var a=[\"monospace\",\"sans-serif\",\"serif\"],r=[\"Andale Mono\",\"Arial\",\"Arial Black\",\"Arial Hebrew\",\"Arial MT\",\"Arial Narrow\",\"Arial Rounded MT Bold\",\"Arial Unicode MS\",\"Bitstream Vera Sans Mono\",\"Book Antiqua\",\"Bookman Old Style\",\"Calibri\",\"Cambria\",\"Cambria Math\",\"Century\",\"Century Gothic\",\"Century Schoolbook\",\"Comic Sans\",\"Comic Sans MS\",\"Consolas\",\"Courier\",\"Courier New\",\"Geneva\",\"Georgia\",\"Helvetica\",\"Helvetica Neue\",\"Impact\",\"Lucida Bright\",\"Lucida Calligraphy\",\"Lucida Console\",\"Lucida Fax\",\"LUCIDA GRANDE\",\"Lucida Handwriting\",\"Lucida Sans\",\"Lucida Sans Typewriter\",\"Lucida Sans Unicode\",\"Microsoft Sans Serif\",\"Monaco\",\"Monotype Corsiva\",\"MS Gothic\",\"MS Outlook\",\"MS PGothic\",\"MS Reference Sans Serif\",\"MS Sans Serif\",\"MS Serif\",\"MYRIAD\",\"MYRIAD PRO\",\"Palatino\",\"Palatino Linotype\",\"Segoe Print\",\"Segoe Script\",\"Segoe UI\",\"Segoe UI Light\",\"Segoe UI Semibold\",\"Segoe UI Symbol\",\"Tahoma\",\"Times\",\"Times New Roman\",\"Times New Roman PS\",\"Trebuchet MS\",\"Verdana\",\"Wingdings\",\"Wingdings 2\",\"Wingdings 3\"];t.fonts.extendedJsFonts&&(r=r.concat([\"Abadi MT Condensed Light\",\"Academy Engraved LET\",\"ADOBE CASLON PRO\",\"Adobe Garamond\",\"ADOBE GARAMOND PRO\",\"Agency FB\",\"Aharoni\",\"Albertus Extra Bold\",\"Albertus Medium\",\"Algerian\",\"Amazone BT\",\"American Typewriter\",\"American Typewriter Condensed\",\"AmerType Md BT\",\"Andalus\",\"Angsana New\",\"AngsanaUPC\",\"Antique Olive\",\"Aparajita\",\"Apple Chancery\",\"Apple Color Emoji\",\"Apple SD Gothic Neo\",\"Arabic Typesetting\",\"ARCHER\",\"ARNO PRO\",\"Arrus BT\",\"Aurora Cn BT\",\"AvantGarde Bk BT\",\"AvantGarde Md BT\",\"AVENIR\",\"Ayuthaya\",\"Bandy\",\"Bangla Sangam MN\",\"Bank Gothic\",\"BankGothic Md BT\",\"Baskerville\",\"Baskerville Old Face\",\"Batang\",\"BatangChe\",\"Bauer Bodoni\",\"Bauhaus 93\",\"Bazooka\",\"Bell MT\",\"Bembo\",\"Benguiat Bk BT\",\"Berlin Sans FB\",\"Berlin Sans FB Demi\",\"Bernard MT Condensed\",\"BernhardFashion BT\",\"BernhardMod BT\",\"Big Caslon\",\"BinnerD\",\"Blackadder ITC\",\"BlairMdITC TT\",\"Bodoni 72\",\"Bodoni 72 Oldstyle\",\"Bodoni 72 Smallcaps\",\"Bodoni MT\",\"Bodoni MT Black\",\"Bodoni MT Condensed\",\"Bodoni MT Poster Compressed\",\"Bookshelf Symbol 7\",\"Boulder\",\"Bradley Hand\",\"Bradley Hand ITC\",\"Bremen Bd BT\",\"Britannic Bold\",\"Broadway\",\"Browallia New\",\"BrowalliaUPC\",\"Brush Script MT\",\"Californian FB\",\"Calisto MT\",\"Calligrapher\",\"Candara\",\"CaslonOpnface BT\",\"Castellar\",\"Centaur\",\"Cezanne\",\"CG Omega\",\"CG Times\",\"Chalkboard\",\"Chalkboard SE\",\"Chalkduster\",\"Charlesworth\",\"Charter Bd BT\",\"Charter BT\",\"Chaucer\",\"ChelthmITC Bk BT\",\"Chiller\",\"Clarendon\",\"Clarendon Condensed\",\"CloisterBlack BT\",\"Cochin\",\"Colonna MT\",\"Constantia\",\"Cooper Black\",\"Copperplate\",\"Copperplate Gothic\",\"Copperplate Gothic Bold\",\"Copperplate Gothic Light\",\"CopperplGoth Bd BT\",\"Corbel\",\"Cordia New\",\"CordiaUPC\",\"Cornerstone\",\"Coronet\",\"Cuckoo\",\"Curlz MT\",\"DaunPenh\",\"Dauphin\",\"David\",\"DB LCD Temp\",\"DELICIOUS\",\"Denmark\",\"DFKai-SB\",\"Didot\",\"DilleniaUPC\",\"DIN\",\"DokChampa\",\"Dotum\",\"DotumChe\",\"Ebrima\",\"Edwardian Script ITC\",\"Elephant\",\"English 111 Vivace BT\",\"Engravers MT\",\"EngraversGothic BT\",\"Eras Bold ITC\",\"Eras Demi ITC\",\"Eras Light ITC\",\"Eras Medium ITC\",\"EucrosiaUPC\",\"Euphemia\",\"Euphemia UCAS\",\"EUROSTILE\",\"Exotc350 Bd BT\",\"FangSong\",\"Felix Titling\",\"Fixedsys\",\"FONTIN\",\"Footlight MT Light\",\"Forte\",\"FrankRuehl\",\"Fransiscan\",\"Freefrm721 Blk BT\",\"FreesiaUPC\",\"Freestyle Script\",\"French Script MT\",\"FrnkGothITC Bk BT\",\"Fruitger\",\"FRUTIGER\",\"Futura\",\"Futura Bk BT\",\"Futura Lt BT\",\"Futura Md BT\",\"Futura ZBlk BT\",\"FuturaBlack BT\",\"Gabriola\",\"Galliard BT\",\"Gautami\",\"Geeza Pro\",\"Geometr231 BT\",\"Geometr231 Hv BT\",\"Geometr231 Lt BT\",\"GeoSlab 703 Lt BT\",\"GeoSlab 703 XBd BT\",\"Gigi\",\"Gill Sans\",\"Gill Sans MT\",\"Gill Sans MT Condensed\",\"Gill Sans MT Ext Condensed Bold\",\"Gill Sans Ultra Bold\",\"Gill Sans Ultra Bold Condensed\",\"Gisha\",\"Gloucester MT Extra Condensed\",\"GOTHAM\",\"GOTHAM BOLD\",\"Goudy Old Style\",\"Goudy Stout\",\"GoudyHandtooled BT\",\"GoudyOLSt BT\",\"Gujarati Sangam MN\",\"Gulim\",\"GulimChe\",\"Gungsuh\",\"GungsuhChe\",\"Gurmukhi MN\",\"Haettenschweiler\",\"Harlow Solid Italic\",\"Harrington\",\"Heather\",\"Heiti SC\",\"Heiti TC\",\"HELV\",\"Herald\",\"High Tower Text\",\"Hiragino Kaku Gothic ProN\",\"Hiragino Mincho ProN\",\"Hoefler Text\",\"Humanst 521 Cn BT\",\"Humanst521 BT\",\"Humanst521 Lt BT\",\"Imprint MT Shadow\",\"Incised901 Bd BT\",\"Incised901 BT\",\"Incised901 Lt BT\",\"INCONSOLATA\",\"Informal Roman\",\"Informal011 BT\",\"INTERSTATE\",\"IrisUPC\",\"Iskoola Pota\",\"JasmineUPC\",\"Jazz LET\",\"Jenson\",\"Jester\",\"Jokerman\",\"Juice ITC\",\"Kabel Bk BT\",\"Kabel Ult BT\",\"Kailasa\",\"KaiTi\",\"Kalinga\",\"Kannada Sangam MN\",\"Kartika\",\"Kaufmann Bd BT\",\"Kaufmann BT\",\"Khmer UI\",\"KodchiangUPC\",\"Kokila\",\"Korinna BT\",\"Kristen ITC\",\"Krungthep\",\"Kunstler Script\",\"Lao UI\",\"Latha\",\"Leelawadee\",\"Letter Gothic\",\"Levenim MT\",\"LilyUPC\",\"Lithograph\",\"Lithograph Light\",\"Long Island\",\"Lydian BT\",\"Magneto\",\"Maiandra GD\",\"Malayalam Sangam MN\",\"Malgun Gothic\",\"Mangal\",\"Marigold\",\"Marion\",\"Marker Felt\",\"Market\",\"Marlett\",\"Matisse ITC\",\"Matura MT Script Capitals\",\"Meiryo\",\"Meiryo UI\",\"Microsoft Himalaya\",\"Microsoft JhengHei\",\"Microsoft New Tai Lue\",\"Microsoft PhagsPa\",\"Microsoft Tai Le\",\"Microsoft Uighur\",\"Microsoft YaHei\",\"Microsoft Yi Baiti\",\"MingLiU\",\"MingLiU_HKSCS\",\"MingLiU_HKSCS-ExtB\",\"MingLiU-ExtB\",\"Minion\",\"Minion Pro\",\"Miriam\",\"Miriam Fixed\",\"Mistral\",\"Modern\",\"Modern No. 20\",\"Mona Lisa Solid ITC TT\",\"Mongolian Baiti\",\"MONO\",\"MoolBoran\",\"Mrs Eaves\",\"MS LineDraw\",\"MS Mincho\",\"MS PMincho\",\"MS Reference Specialty\",\"MS UI Gothic\",\"MT Extra\",\"MUSEO\",\"MV Boli\",\"Nadeem\",\"Narkisim\",\"NEVIS\",\"News Gothic\",\"News GothicMT\",\"NewsGoth BT\",\"Niagara Engraved\",\"Niagara Solid\",\"Noteworthy\",\"NSimSun\",\"Nyala\",\"OCR A Extended\",\"Old Century\",\"Old English Text MT\",\"Onyx\",\"Onyx BT\",\"OPTIMA\",\"Oriya Sangam MN\",\"OSAKA\",\"OzHandicraft BT\",\"Palace Script MT\",\"Papyrus\",\"Parchment\",\"Party LET\",\"Pegasus\",\"Perpetua\",\"Perpetua Titling MT\",\"PetitaBold\",\"Pickwick\",\"Plantagenet Cherokee\",\"Playbill\",\"PMingLiU\",\"PMingLiU-ExtB\",\"Poor Richard\",\"Poster\",\"PosterBodoni BT\",\"PRINCETOWN LET\",\"Pristina\",\"PTBarnum BT\",\"Pythagoras\",\"Raavi\",\"Rage Italic\",\"Ravie\",\"Ribbon131 Bd BT\",\"Rockwell\",\"Rockwell Condensed\",\"Rockwell Extra Bold\",\"Rod\",\"Roman\",\"Sakkal Majalla\",\"Santa Fe LET\",\"Savoye LET\",\"Sceptre\",\"Script\",\"Script MT Bold\",\"SCRIPTINA\",\"Serifa\",\"Serifa BT\",\"Serifa Th BT\",\"ShelleyVolante BT\",\"Sherwood\",\"Shonar Bangla\",\"Showcard Gothic\",\"Shruti\",\"Signboard\",\"SILKSCREEN\",\"SimHei\",\"Simplified Arabic\",\"Simplified Arabic Fixed\",\"SimSun\",\"SimSun-ExtB\",\"Sinhala Sangam MN\",\"Sketch Rockwell\",\"Skia\",\"Small Fonts\",\"Snap ITC\",\"Snell Roundhand\",\"Socket\",\"Souvenir Lt BT\",\"Staccato222 BT\",\"Steamer\",\"Stencil\",\"Storybook\",\"Styllo\",\"Subway\",\"Swis721 BlkEx BT\",\"Swiss911 XCm BT\",\"Sylfaen\",\"Synchro LET\",\"System\",\"Tamil Sangam MN\",\"Technical\",\"Teletype\",\"Telugu Sangam MN\",\"Tempus Sans ITC\",\"Terminal\",\"Thonburi\",\"Traditional Arabic\",\"Trajan\",\"TRAJAN PRO\",\"Tristan\",\"Tubular\",\"Tunga\",\"Tw Cen MT\",\"Tw Cen MT Condensed\",\"Tw Cen MT Condensed Extra Bold\",\"TypoUpright BT\",\"Unicorn\",\"Univers\",\"Univers CE 55 Medium\",\"Univers Condensed\",\"Utsaah\",\"Vagabond\",\"Vani\",\"Vijaya\",\"Viner Hand ITC\",\"VisualUI\",\"Vivaldi\",\"Vladimir Script\",\"Vrinda\",\"Westminster\",\"WHITNEY\",\"Wide Latin\",\"ZapfEllipt BT\",\"ZapfHumnst BT\",\"ZapfHumnst Dm BT\",\"Zapfino\",\"Zurich BlkEx BT\",\"Zurich Ex BT\",\"ZWAdobeF\"]));r=(r=r.concat(t.fonts.userDefinedFonts)).filter(function(e,t){return r.indexOf(e)===t});var n=document.getElementsByTagName(\"body\")[0],i=document.createElement(\"div\"),o=document.createElement(\"div\"),l={},s={},c=function(){var e=document.createElement(\"span\");return e.style.position=\"absolute\",e.style.left=\"-9999px\",e.style.fontSize=\"72px\",e.style.fontStyle=\"normal\",e.style.fontWeight=\"normal\",e.style.letterSpacing=\"normal\",e.style.lineBreak=\"auto\",e.style.lineHeight=\"normal\",e.style.textTransform=\"none\",e.style.textAlign=\"left\",e.style.textDecoration=\"none\",e.style.textShadow=\"none\",e.style.whiteSpace=\"normal\",e.style.wordBreak=\"normal\",e.style.wordSpacing=\"normal\",e.innerHTML=\"mmmmmmmmmmlli\",e},u=function(e,t){var a=c();return a.style.fontFamily=\"'\"+e+\"',\"+t,a},d=function(e){for(var t=!1,r=0;r<a.length;r++)if(t=e[r].offsetWidth!==l[a[r]]||e[r].offsetHeight!==s[a[r]])return t;return t},g=function(){for(var e=[],t=0,r=a.length;t<r;t++){var n=c();n.style.fontFamily=a[t],i.appendChild(n),e.push(n)}return e}();n.appendChild(i);for(var h=0,T=a.length;h<T;h++)l[a[h]]=g[h].offsetWidth,s[a[h]]=g[h].offsetHeight;var m=function(){for(var e={},t=0,n=r.length;t<n;t++){for(var i=[],l=0,s=a.length;l<s;l++){var c=u(r[t],a[l]);o.appendChild(c),i.push(c)}e[r[t]]=i}return e}();n.appendChild(o);for(var f=[],p=0,S=r.length;p<S;p++)d(m[r[p]])&&f.push(r[p]);n.removeChild(o),n.removeChild(i),e(f)},pauseBefore:!0},{key:\"ua\",getData:function(e){e(navigator.userAgent)}}],S={preprocessor:null,audio:{timeout:1e3,excludeIOS11:!0},fonts:{userDefinedFonts:[],extendedJsFonts:!1},screen:{detectScreenOrientation:!0},plugins:{sortPluginsFor:[/palemoon/i],excludeIE:!1},extraComponents:[],excludes:{enumerateDevices:!0,pixelRatio:!0,doNotTrack:!0,fontsFlash:!0},NOT_AVAILABLE:\"not available\",ERROR:\"error\",EXCLUDED:\"excluded\"},C=function(e){throw new Error(\"'new Fingerprint()' is deprecated\")};return C.get=function(e,t){t?e||(e={}):(t=e,e={}),function(e,t){if(null==t)return e;var a,r;for(r in t)null==(a=t[r])||Object.prototype.hasOwnProperty.call(e,r)||(e[r]=a)}(e,S),e.components=e.extraComponents.concat(p);var a={data:[],addPreprocessedComponent:function(t,r){\"function\"==typeof e.preprocessor&&(r=e.preprocessor(t,r)),a.data.push({key:t,value:r})}},r=-1,n=function(i){if((r+=1)>=e.components.length)t(a.data);else{var o=e.components[r];if(e.excludes[o.key])n(!1);else{if(!i&&o.pauseBefore)return r-=1,void setTimeout(function(){n(!0)},1);try{o.getData(function(e){a.addPreprocessedComponent(o.key,e),n(!1)},e)}catch(e){a.addPreprocessedComponent(o.key,String(e)),n(!1)}}}};n(!1)},C.x64hash128=function(o,l){l=l||0;for(var s=(o=o||\"\").length%16,c=o.length-s,u=[0,l],d=[0,l],g=[0,0],h=[0,0],T=[2277735313,289559509],m=[1291169091,658871167],f=0;f<c;f+=16)g=[255&o.charCodeAt(f+4)|(255&o.charCodeAt(f+5))<<8|(255&o.charCodeAt(f+6))<<16|(255&o.charCodeAt(f+7))<<24,255&o.charCodeAt(f)|(255&o.charCodeAt(f+1))<<8|(255&o.charCodeAt(f+2))<<16|(255&o.charCodeAt(f+3))<<24],h=[255&o.charCodeAt(f+12)|(255&o.charCodeAt(f+13))<<8|(255&o.charCodeAt(f+14))<<16|(255&o.charCodeAt(f+15))<<24,255&o.charCodeAt(f+8)|(255&o.charCodeAt(f+9))<<8|(255&o.charCodeAt(f+10))<<16|(255&o.charCodeAt(f+11))<<24],g=e(g,T),g=t(g,31),g=e(g,m),u=a(u,g),u=t(u,27),u=i(u,d),u=i(e(u,[0,5]),[0,1390208809]),h=e(h,m),h=t(h,33),h=e(h,T),d=a(d,h),d=t(d,31),d=i(d,u),d=i(e(d,[0,5]),[0,944331445]);switch(g=[0,0],h=[0,0],s){case 15:h=a(h,r([0,o.charCodeAt(f+14)],48));case 14:h=a(h,r([0,o.charCodeAt(f+13)],40));case 13:h=a(h,r([0,o.charCodeAt(f+12)],32));case 12:h=a(h,r([0,o.charCodeAt(f+11)],24));case 11:h=a(h,r([0,o.charCodeAt(f+10)],16));case 10:h=a(h,r([0,o.charCodeAt(f+9)],8));case 9:h=a(h,[0,o.charCodeAt(f+8)]),h=e(h,m),h=t(h,33),h=e(h,T),d=a(d,h);case 8:g=a(g,r([0,o.charCodeAt(f+7)],56));case 7:g=a(g,r([0,o.charCodeAt(f+6)],48));case 6:g=a(g,r([0,o.charCodeAt(f+5)],40));case 5:g=a(g,r([0,o.charCodeAt(f+4)],32));case 4:g=a(g,r([0,o.charCodeAt(f+3)],24));case 3:g=a(g,r([0,o.charCodeAt(f+2)],16));case 2:g=a(g,r([0,o.charCodeAt(f+1)],8));case 1:g=a(g,[0,o.charCodeAt(f)]),g=e(g,T),g=t(g,31),g=e(g,m),u=a(u,g)}return u=a(u,[0,o.length]),d=a(d,[0,o.length]),u=i(u,d),d=i(d,u),u=n(u),d=n(d),u=i(u,d),d=i(d,u),(\"00000000\"+(u[0]>>>0).toString(16)).slice(-8)+(\"00000000\"+(u[1]>>>0).toString(16)).slice(-8)+(\"00000000\"+(d[0]>>>0).toString(16)).slice(-8)+(\"00000000\"+(d[1]>>>0).toString(16)).slice(-8)},C.VERSION=\"2.0.0\",C});" +
            "\n" +
            "function jsCallOCReturnMethod(){Fingerprint.get(function(t){var r={ca:Fingerprint.x64hash128(t[0].value.toString(),31),wg:Fingerprint.x64hash128(t[1].value.toString(),31),pi:Fingerprint.x64hash128(t[2].value.toString(),31),ao:Fingerprint.x64hash128(t[3].value.toString(),31),se:Fingerprint.x64hash128(t[4].value.toString(),31),ft:Fingerprint.x64hash128(t[5].value.toString(),31),ua:t[6].value.toString()},n=JSON.stringify(r);setTimeout(()=>{try{window.prompt(\"jsCallOCReturnJsonStringMethod\",n);window.AndroidInterface.receiveParameterFromHTML(n)}catch(t){console.error(t)}},500)})}</script></html>";
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadDataWithBaseURL(null, htmlStr, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建一个内部类，用于实现JavaScript接口
    private class JavaScriptInterface {
        @JavascriptInterface
        public void receiveParameterFromHTML(String parameter) {
            // 接收从HTML传递过来的参数
            mParameter = parameter;
            // 将参数保存到SharedPreferences中
            saveParameterToSharedPreferences(parameter);
        }
    }

    private void saveParameterToSharedPreferences(String parameter) {
        if (mContext != null) {
            try {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("HM_WebView_Fingerprint", parameter);
                editor.apply();
                Log.d("HM_GET_FINGERPRINT", "Fingerprint: " + parameter);
                // 只有当回调接口不为空时才触发回调
                if (fingerprintCallback != null && parameter != null) {
                    fingerprintCallback.onCallback(parameter);
                    fingerprintCallback = null; // 将回调置为null，以防止再次触发
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendJSONString() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebView.loadUrl("javascript:jsCallOCReturnMethod()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void GetFingerprint(FingerprintCallback callback) {
        if (callback != null) {
            fingerprintCallback = callback;
        }
    }

    // 回调接口
    public interface FingerprintCallback {
        void onCallback(String Fingerprint); // 回调方法，接收数组和字符串参数
    }
}
