using DrawMap.Domain;

namespace DrawMap.Repositories;

public interface ILocationRepositories
{
    List<Location> GetLocations();
}