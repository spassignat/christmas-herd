generate test to execute the AnimalResource.image, to ensure:
- at least one is online
- at least one is offline

Load available implementation using the ServiceLoader:
ServiceLoader.load(ResponseAdapter.class, ResponseAdapter.class.getClassLoader())
