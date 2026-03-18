using DrawMap.DataSources;
using DrawMap.Domain;

namespace DrawMap.Repositories;

public class LocationRepositories : ILocationRepositories
{
    private readonly DrawMapDbContext _context;

    public LocationRepositories(DrawMapDbContext context)
    {
        _context = context;
    }
    
    public List<Location> GetLocations()
    {
        return _context.Locations.ToList();
    }
}