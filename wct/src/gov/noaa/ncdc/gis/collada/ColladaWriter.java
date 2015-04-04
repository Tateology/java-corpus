package gov.noaa.ncdc.gis.collada;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Writer for Google Earth COLLADA (.dae) files.
 * 
 * 
 * FROM GOOGLE EARTH DOCS:
 * http://code.google.com/apis/earth/documentation/reference/interface_kml_model.html
 * 
 * A 3D object described in a referenced COLLADA file. COLLADA files have a .dae file extension. 
 * Models are created in their own coordinate space and then located, positioned, and scaled in 
 * Google Earth. Google Earth supports the COLLADA common profile, with the following exceptions:
 *
 *   * Google Earth supports only triangles and lines as primitive types. The maximum number of 
 *   	triangles allowed is 21845.
 *   * Google Earth does not support animation or skinning.
 *   * Google Earth does not support external geometry references. 
 *
 * @author steve.ansari
 *
 */
public class ColladaWriter {

	private ColladaCalculator collada = null;
	private String textureImageFile = null;
	
	
	public String createColladaXML(ColladaCalculator collada, String textureImageFile) {
		
		this.collada = collada;
		this.textureImageFile = textureImageFile;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		sb.append("<COLLADA xmlns=\"http://www.collada.org/2005/11/COLLADASchema\" version=\"1.4.1\"> \n");

		sb.append(getSectionAssets());
		sb.append(getSectionLibraryImages());
		sb.append(getSectionLibraryMaterials());
		sb.append(getSectionLibraryEffects());
		
		sb.append(getSectionLibraryGeometries());

		sb.append(getSectionLibraryCameras());
		sb.append(getSectionLibraryLights());
		sb.append(getSectionLibraryVisualScenes());
		sb.append(getSectionLibraryScene());
		
		sb.append("</COLLADA>\n");
		
		return sb.toString();
	}
	
	private String getSectionAssets() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<asset> \n");
		sb.append(" <contributor> \n");
		sb.append("	 <copyright>NONE - Public Domain U.S. Federal Government Code</copyright> \n");
		sb.append("	 <comments> COLLADA programmatically produced </comments> \n");
		sb.append("		<author>Steve Ansari NOAA/National Climatic Data Center ( Steve.Ansari@noaa.gov )</author> \n");
		sb.append("		<authoring_tool>NOAA Weather and Climate Toolkit</authoring_tool> \n");
		sb.append("	 </contributor> \n");
		sb.append("	 <created></created> \n"); // ISO FORMAT DATE/TIME
		sb.append("	 <modified></modified> \n");
		sb.append("	 <unit name=\"meters\" meter=\"1.0\" /> \n");
		sb.append("	 <up_axis>Z_UP</up_axis> \n");
		sb.append("	</asset> \n");

		return sb.toString();
	}
	
	private String getSectionLibraryImages() {
		StringBuilder sb = new StringBuilder();
		sb.append("<library_images> \n");
		sb.append(" <image id=\"texture-image\" name=\"texture-image\"> \n");
		sb.append("  <init_from>"+textureImageFile+"</init_from> \n");
		sb.append(" </image> \n");
		sb.append("</library_images> \n");

		return sb.toString();
	}
	
	
	private String getSectionLibraryMaterials() {
		StringBuilder sb = new StringBuilder();
		sb.append("<library_materials> \n");
		sb.append(" <material id=\"textureID\" name=\"texture\"> ");
		sb.append("  <instance_effect url=\"#texture-effect\" /> \n");
		sb.append(" </material> \n");
		sb.append("</library_materials> \n");
		return sb.toString();
	}
	
	private String getSectionLibraryCameras() {
		StringBuilder sb = new StringBuilder();
		sb.append("<library_cameras> \n");
		sb.append("<camera id=\"Camera-camera\" name=\"Camera-camera\"> \n");
		sb.append("	<optics> \n");
		sb.append("  <technique_common> \n");
		sb.append("   <perspective> \n");
		sb.append("		<xfov>40.000000</xfov> \n");
		sb.append("     <yfov>30.000000</yfov> \n");
		sb.append("     <znear>0.025400</znear> \n");
		sb.append("     <zfar>25.400000</zfar> \n");
		sb.append("   </perspective> \n");
		sb.append("  </technique_common> \n");
		sb.append(" </optics> \n");
		sb.append("</camera> \n");
		sb.append("</library_cameras> \n");

		return sb.toString();
	}
	
	private String getSectionLibraryLights() {
		StringBuilder sb = new StringBuilder();
		sb.append("<library_lights> \n");
		sb.append(" <light id=\"defaultambient-light\" name=\"defaultambient\"> \n");
		sb.append("  <technique_common> \n");
		sb.append("   <ambient> \n");
		sb.append("    <color>1.000000 1.000000 1.000000</color> \n");
		sb.append("   </ambient> \n");
		sb.append("  </technique_common> \n");
		sb.append(" </light> \n");
		sb.append(" <light id=\"light-light\" name=\"light\"> \n");
		sb.append("  <technique_common> \n");
		sb.append("   <directional> \n");
		sb.append("    <color>1.000000 1.000000 1.000000</color> \n");
		sb.append("   </directional> \n");
		sb.append("  </technique_common> \n");
		sb.append(" </light> \n");
		sb.append("<light id=\"light-light_1\" name=\"light_1\"> \n");
		sb.append(" <technique_common> \n");
		sb.append("  <directional> \n");
		sb.append("   <color>1.000000 1.000000 1.000000</color> \n");
		sb.append("  </directional> \n");
		sb.append(" </technique_common> \n");
		sb.append("</light> \n");
		sb.append("</library_lights> \n");
		return sb.toString();
	}
	
	private String getSectionLibraryVisualScenes() {
		StringBuilder sb = new StringBuilder();
		sb.append("<library_visual_scenes> ");
		sb.append(" <visual_scene id=\"SketchUpScene\" name=\"SketchUpScene\"> \n");
		sb.append("  <node id=\"Model\" name=\"Model\"> \n");
		sb.append("   <node id=\"TIN\" name=\"TIN\"> \n");
		sb.append("    <node id=\"mesh1\" name=\"mesh1\"> \n");
		sb.append("     <instance_geometry url=\"#mesh1-geometry\"> \n");
		sb.append("      <bind_material> \n");
		sb.append("       <technique_common> \n");
		sb.append("        <instance_material symbol=\"texture\" target=\"#textureID\"> \n");
	    sb.append("         <bind_vertex_input semantic=\"UVSET0\" input_semantic=\"TEXCOORD\" input_set=\"0\" /> \n");
	    sb.append("        </instance_material> \n");
	    sb.append("       </technique_common> \n");
	    sb.append("      </bind_material> \n");
	    sb.append("     </instance_geometry> \n");
	    sb.append("    </node> \n");
	    sb.append("   </node> \n");
	    sb.append("  </node> \n");
	    sb.append("  <node id=\"Camera\" name=\"Camera\"> \n");
	    sb.append("   <matrix>0.999113 0.034034 -0.024788 21.486919-0.042104 0.807612 \n");
	    sb.append("          -0.588209 -628.881023-0.000000 0.588731 0.808329 966.2483220.000000 \n");
	    sb.append("          0.000000 0.000000 1.000000</matrix> \n");
	    sb.append("   <instance_camera url=\"#Camera-camera\" /> \n");
	    sb.append("  </node> \n");
	    sb.append(" </visual_scene> \n");
	    sb.append("</library_visual_scenes> \n");
		
		return sb.toString();
	}
	
	private String getSectionLibraryScene() {
		StringBuilder sb = new StringBuilder();
		sb.append("<scene> \n");
		sb.append("<instance_visual_scene url=\"#SketchUpScene\" /> \n");
		sb.append("</scene> \n");

		return sb.toString();
	}

	
	private String getSectionLibraryGeometries() {
		StringBuilder sb = new StringBuilder();
		sb.append("	<library_geometries> \n");
		sb.append("	 <geometry id=\"mesh1-geometry\" name=\"mesh1-geometry\"> \n");
		sb.append("	  <mesh> \n");

		sb.append(getSectionGeometryPositions());
		sb.append(getSectionGeometryNormals());
		sb.append(getSectionGeometryUVs());
		sb.append(getSectionTriangles());
		
		sb.append("	  </mesh> \n");
		sb.append("	 </geometry> \n");
		sb.append("	</library_geometries> \n");

		return sb.toString();
	}
	
	
	private String getSectionGeometryPositions() {
		StringBuilder sb = new StringBuilder();
		sb.append("    <source id=\"mesh1-geometry-position\"> \n");
		sb.append("     <float_array id=\"mesh1-geometry-position-array\" count=\""+collada.getCoordinates().length * 3 +"\"> \n");
		for (Coordinate coord : collada.getCoordinates()) {
			sb.append(coord.x).append(" ").append(coord.y).append(" ").append(coord.z).append("\n");
		}
		sb.append("     </float_array> \n");
		sb.append("     <technique_common> \n");
		sb.append("     	<accessor source=\"#mesh1-geometry-position-array\" count=\""+collada.getCoordinates().length +"\" stride=\"3\"> \n");
		sb.append("     		<param name=\"X\" type=\"float\" /> \n");
		sb.append("     		<param name=\"Y\" type=\"float\" /> \n");
		sb.append("     		<param name=\"Z\" type=\"float\" /> \n");
		sb.append("     	</accessor> \n");
		sb.append("     </technique_common> \n");
		sb.append("    </source> \n");
		
		return sb.toString();
	}

	
	private String getSectionGeometryNormals() {
		StringBuilder sb = new StringBuilder();
		sb.append("    <source id=\"mesh1-geometry-normal\"> \n");
		sb.append("     <float_array id=\"mesh1-geometry-normal-array\" count=\""+collada.getCoordVertexNormals().length*3 +"\"> \n");
		for (double[] vertNormals : collada.getCoordVertexNormals()) {
			sb.append(vertNormals[0]).append(" ").append(vertNormals[1]).append(" ").append(vertNormals[2]).append("\n");
		}
		sb.append("     </float_array> \n");
		sb.append("     <technique_common> \n");
		sb.append("     	<accessor source=\"#mesh1-geometry-normal-array\" count=\""+collada.getCoordVertexNormals().length +"\" stride=\"3\"> \n");
		sb.append("     		<param name=\"X\" type=\"float\" /> \n");
		sb.append("     		<param name=\"Y\" type=\"float\" /> \n");
		sb.append("     		<param name=\"Z\" type=\"float\" /> \n");
		sb.append("     	</accessor> \n");
		sb.append("     </technique_common> \n");
		sb.append("    </source> \n");
		
		return sb.toString();
	}

	private String getSectionGeometryUVs() {
		StringBuilder sb = new StringBuilder();
		sb.append("    <source id=\"mesh1-geometry-uv\"> \n");
		sb.append("     <float_array id=\"mesh1-geometry-uv-array\" count=\""+collada.getCoordVertexUV().length*2 +"\"> \n");
		for (double[] vertUV : collada.getCoordVertexUV()) {
			sb.append(vertUV[0]).append(" ").append(vertUV[1]).append("\n");
		}
		sb.append("     </float_array> \n");
		sb.append("     <technique_common> \n");
		sb.append("     	<accessor source=\"#mesh1-geometry-normal-array\" count=\""+collada.getCoordVertexUV().length +"\" stride=\"2\"> \n");
		sb.append("     		<param name=\"S\" type=\"float\" /> \n");
		sb.append("     		<param name=\"T\" type=\"float\" /> \n");
		sb.append("     	</accessor> \n");
		sb.append("     </technique_common> \n");
		sb.append("    </source> \n");
		
		return sb.toString();
	}

	
	private String getSectionTriangles() {
		StringBuilder sb = new StringBuilder();
		sb.append("    <vertices id=\"mesh1-geometry-vertex\">  \n");
		sb.append("      <input semantic=\"POSITION\" source=\"#mesh1-geometry-position\" /> \n");
		sb.append("    </vertices> \n");
		sb.append("    <triangles material=\"texture\" count=\""+collada.getTriangles().size()+"\"> \n");
		sb.append("      <input semantic=\"VERTEX\" source=\"#mesh1-geometry-vertex\" offset=\"0\" /> \n");
		sb.append("      <input semantic=\"NORMAL\" source=\"#mesh1-geometry-normal\" offset=\"1\" /> \n");
		sb.append("      <input semantic=\"TEXCOORD\" source=\"#mesh1-geometry-uv\"   offset=\"2\" set=\"0\" /> \n");
		sb.append("      <p>\n");

		for (int[] triVertices : collada.getTriangles()) {
			sb.append(triVertices[0]).append(" ").append(triVertices[0]).append(" ").append(triVertices[0]).append(" ");
			sb.append(triVertices[1]).append(" ").append(triVertices[1]).append(" ").append(triVertices[1]).append(" ");
			sb.append(triVertices[2]).append(" ").append(triVertices[2]).append(" ").append(triVertices[2]).append(" \n");
		}
		
		sb.append("      </p>\n");
		sb.append("    </triangles> \n");
		
		return sb.toString();
	}
	
	
	
	private String getSectionLibraryEffects() {
		StringBuilder sb = new StringBuilder();
		
		// more info on these effects at:
		// http://www.okino.com/conv/exp_collada_materials_panel.htm
		
		sb.append("	<library_effects> \n");
		sb.append("		<effect id=\"texture-effect\" name=\"texture-effect\"> \n");
		sb.append("			<profile_COMMON>                                                      \n");
		sb.append("				<newparam sid=\"texture-image-surface\">                \n");
		sb.append("					<surface type=\"2D\">                                         \n");
		sb.append("						<init_from>texture-image</init_from>            \n");
		sb.append("					</surface>                                                    \n");
		sb.append("				</newparam>                                                       \n");
		sb.append("				<newparam sid=\"texture-image-sampler\">                \n");
		sb.append("					<sampler2D>                                                   \n");
		sb.append("						<source>texture-image-surface</source>          \n");
		sb.append("					</sampler2D>                                                  \n");
		sb.append("				</newparam>                                                       \n");
		sb.append("				<technique sid=\"COMMON\">                                        \n");
		sb.append("					<phong>                                                       \n");
		sb.append("						<emission>                                                \n");
		sb.append("							<color>0.000000 0.000000 0.000000 1</color>           \n");
		sb.append("						</emission>                                               \n");
		sb.append("						<ambient>                                                 \n");
		sb.append("							<color>0.300000 0.300000 0.300000 1</color>           \n");
		sb.append("						</ambient>                                                \n");
		sb.append("						<diffuse>                                                 \n");
		sb.append("							<texture texture=\"texture-image-sampler\"  \n");
		sb.append("								texcoord=\"UVSET0\" />                            \n");
		sb.append("						</diffuse>                                                \n");
		sb.append("						<specular>                                                \n");
		sb.append("							<color>0.330000 0.330000 0.330000 1</color>           \n");
		sb.append("						</specular>                                               \n");
		sb.append("						<shininess>                                               \n");
		sb.append("							<float>20.000000</float>                              \n");
		sb.append("						</shininess>                                              \n");
		sb.append("						<reflectivity>                                            \n");
		sb.append("							<float>0.100000</float>                               \n");
		sb.append("						</reflectivity>                                           \n");
		sb.append("						<transparent>                                             \n");
		sb.append("							<color>1 1 1 1</color>                                \n");
		sb.append("						</transparent>                                            \n");
		sb.append("						<transparency>                                            \n");
		sb.append("							<float>0.100000</float>                               \n");
		sb.append("						</transparency>                                           \n");
		sb.append("					</phong>                                                      \n");
		sb.append("				</technique>                                                      \n");
		sb.append("				<extra>                                                           \n");
		sb.append("					<technique profile=\"GOOGLEEARTH\">                           \n");
		sb.append("						<double_sided>1</double_sided>                            \n");
		sb.append("					</technique>                                                  \n");
		sb.append("				</extra>                                                          \n");
		sb.append("			</profile_COMMON>                                                     \n");
		sb.append("		</effect>                                                                 \n");
		sb.append("	</library_effects>                                                            \n");
		
		return sb.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		
		try {
		
			Coordinate[] coords = new Coordinate[] {
					new Coordinate(10, 10, 10),
					new Coordinate(20, 10, 10),
					new Coordinate(20, 20, 10),
					new Coordinate(10, 20, 10)
			};		
			//			3  2
			//			0  1


			ArrayList<int[]> triangleList = new ArrayList<int[]>();
			triangleList.add( new int[] { 0 , 1 , 2 } );
			triangleList.add( new int[] { 0 , 2 , 3 } );




			ColladaCalculator collada = new ColladaCalculator();
			collada.loadData(coords, triangleList);

			ColladaWriter writer = new ColladaWriter();
			String xml = writer.createColladaXML(collada, null);

			System.out.println(xml);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
