using DrawMap.Domain;
using DrawMap.Repositories;
using DrawMap.WebAPI.DTO;

namespace DrawMap.WebAPI.Services;

public class HeatMapService : IHeatMapService
{
    private readonly ILocationRepositories _locationRepositories;

    public HeatMapService(ILocationRepositories locationRepositories)
    {
        _locationRepositories = locationRepositories;
    }
    
    public Task<List<HeatMap>> GetHeatMap(CancellationToken cancellationToken)
    {
        var locations = _locationRepositories.GetLocations();
        
        const double tolerance = 0.0001;
        
        var groupedLocations = new Dictionary<LocationDto, int>();
        
        foreach (var location in locations)
        {
            var roundedLat = Math.Round(location.Latitude / tolerance) * tolerance;
            var roundedLon = Math.Round(location.Longitude / tolerance) * tolerance;
            
            var key = new LocationDto
            {
                Latitude = roundedLat,
                Longitude = roundedLon,
            };
            
            if (!groupedLocations.TryAdd(key, 1))
            {
                groupedLocations[key]++;
            }
        }
        
        var heatMaps = groupedLocations.Select(kvp => new HeatMap
        {
            Location = kvp.Key,
            VisitFrequency = kvp.Value
        }).ToList();
        
        return Task.FromResult(heatMaps);
    }
}