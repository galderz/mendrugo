import jdk.vm.ci.services.Services;

public class JvmCiServices
{
    public static void main(String[] args)
    {
        System.out.println(Services.getSavedProperties().get("java.specification.version"));
    }
}
