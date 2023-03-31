import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String csvFile = "data.csv";
        String xmlFile = "data.xml";

        List<Employee> listFromCSV = parseCSV(columnMapping, csvFile);
        String json = listToJSON(listFromCSV);
        writeString(json, "data.json");

        List<Employee> listFromXML = parseXML(xmlFile);
        String xml = listToJSON(listFromXML);
        writeString(xml, "data2.json");

        String jsonString = readString("data.json");
        List<Employee> list = jsonToList(jsonString);
        list.forEach(System.out::println);

    }

    public static void writeString(String string,String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(string);
            writer.flush();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static String listToJSON(List<Employee> employees) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(employees, listType);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String name) {
        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping(columnMapping);

        try (CSVReader reader = new CSVReader(new FileReader(name))) {
            CsvToBean<Employee> parser = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            return parser.parse();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    public static List<Employee> parseXML(String name) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(name));

        List<Employee> employees = new ArrayList<>();

        Node root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);

            if (Node.ELEMENT_NODE == child.getNodeType()) {
                Element element = (Element) child;
                Employee employee = new Employee();

                employee.setId(Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent()));
                employee.setFirstName(element.getElementsByTagName("firstName").item(0).getTextContent());
                employee.setLastName(element.getElementsByTagName("lastName").item(0).getTextContent());
                employee.setCountry(element.getElementsByTagName("country").item(0).getTextContent());
                employee.setAge(Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent()));

                employees.add(employee);
            }
        }
        return employees;
    }

    public static String readString(String fileName) {
        StringBuilder string = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String strg;
            while ((strg = reader.readLine()) != null) {
                string.append(strg);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        return string.toString();
    }

    public static List<Employee> jsonToList(String string) {
        List<Employee> employees =new ArrayList<>();

        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(string);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                employees.add(gson.fromJson(jsonObject.toJSONString(), Employee.class));
            }

        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
        }

        return employees;
    }
}