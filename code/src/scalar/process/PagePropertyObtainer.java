package scalar.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;

import scalar.entity.PageProperty;

/**
 * 该类用来从scalar平台上获取页面和图片的信息
 * @author wan jing
 *
 */
/**
 * @author jwan
 *
 */
public class PagePropertyObtainer {

	/**
	 * 该方法用来从scalar平台获取所有页面的信息
	 * 需要进一步考虑数据量大的情况下如何处理
	 */
	public void getAllPage() {
        // 命令格式：http://scalar.usc.edu/works/virtual-museum-of-american-art/rdf/instancesof/page?rec=0&ref=0
		String url = "http://scalar.usc.edu/works/virtual-museum-of-american-art/rdf/instancesof/page";
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("rec", "0");
		data.put("ref", "0");
		
		// 向scalar平台提交数据
		try {
			postAndParse(url, (new ScalarPageManager()).generateContent(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * 该方法配合getAllPage使用，向scalar服务器发送请求，解析返回的结果
	 * 保存到PageProperty中
	 * @param urlString 发送请求的url
	 * @param content 发送的内容
     * @throws IOException
	 */
	public void postAndParse(String urlString, String content)
			throws IOException
	{
		//提交数据
		URLConnection urlConn=(new ScalarPageManager()).post(urlString, content);
		//获得返回数据
		BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		// Get response data.
		input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		
		String str;
		// 针对每一行内容，进行解析
		while (null != ((str = input.readLine()))) 
		{
			System.out.println("正在处理:" + str);
			if (str.indexOf("<sioc:content>") < 0) //由于sioc：content包含页面内容，内容较多，所以不解析页面内容的行
			{
				// 解析得到每个页面的urn：scalar：version，保存到UrnScalarVersionList中
				int beginIndex = str.indexOf("urn:scalar:version");
				int endIndex = str.indexOf("\"/>");
				if (beginIndex >= 0) {
					System.out.println("解析得到:"+ str.substring(beginIndex, endIndex));
					PageProperty.UrnScalarVersionList.add(str.substring(beginIndex, endIndex));//
				}
			}
	    }
		input.close();
	}
	
	
	/**
	 * 该方法用来获取所有图片的Resource和Version
	 */
	public  void getArtistImageResourceVersion() 
	{
		//命令格式： http://scalar.usc.edu/works/virtual-museum-of-american-art/rdf/instancesof/media?rec=0&ref=0
		String url = PageProperty.BASE_URL + "/rdf/instancesof/media";
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("rec", "0");
		data.put("ref", "0");

		//向scalar平台提交数据
		try {
			postAndParseImage(url, (new ScalarPageManager()).generateContent(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 该方法配合配合getArtistImageResourceVersion使用，向服务器提交请求，解析返回的结果
	 * 并保存到PageProperty
	 * Scalar平台返回的典型的Image数据如下
	 * <rdf:Description rdf:about="http://scalar.usc.edu/works/virtual-museum-of-american-art/1909743_1ajpg.1"> 
	 *    <ov:versionnumber>1</ov:versionnumber>
	 *    <dcterms:title>1909.7.43_1a.jpg</dcterms:title> 
	 *    <art:url rdf:resource="http://americanart.si.edu/images/1909/1909.7.43_1a.jpg"/>
	 *    <scalar:defaultView>plain</scalar:defaultView> 
	 *    <foaf:homepage rdf:resource="http://scalar.usc.edu/works/virtual-museum-of-american-art/users/3551"/>
	 *    <dcterms:created>2014-06-26T16:55:35-07:00</dcterms:created>
	 *    <scalar:urn rdf:resource="urn:scalar:version:189826"/>
	 *    <dcterms:isVersionOf rdf:resource="http://scalar.usc.edu/works/virtual-museum-of-american-art/1909743_1ajpg" /> 
	 *    <rdf:type rdf:resource="http://scalar.usc.edu/2012/01/scalar-ns#Version"/>
	 * </rdf:Description> 
	 * 
	 * @param urlString 图片的url
	 * @param content 需要解析的内容
	 * @throws IOException
	 */
	public  void postAndParseImage(String urlString, String content)
			throws IOException {
		//提交数据
		URLConnection urlConn=(new ScalarPageManager()).post(urlString, content);
		//获得返回数据
	    BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		
	    //数据解析
	    String str;
        String imageUrl ="";
		// 针对每一行内容，进行解析
		while (null != ((str = input.readLine()))) 
		{
			System.out.println("正在处理:" + str);
			//获取url
			int urlBeginIndex = str.indexOf("http://americanart.si.edu/images");
			int urlEndIndex = 0;
			if (urlBeginIndex >= 0) {
				urlEndIndex = str.indexOf("\"/>");
				imageUrl = str.substring(urlBeginIndex, urlEndIndex);
				System.out.println("imageurl:"+imageUrl);
			}

			//获取version
			int versionBeginPosition = str.indexOf("urn:scalar:version");
			int versionEndPosition = str.indexOf("\"/>");
			if (versionBeginPosition >= 0) {
				System.out.println("version:"+str.substring(versionBeginPosition, versionEndPosition));
				PageProperty.ArtistImageResourceVersionMap.put(imageUrl,
						str.substring(versionBeginPosition, versionEndPosition));
			}
		}
		input.close();
	}
}
