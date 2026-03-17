using DrawMap.Domain;

namespace DrawMap.WebAPI.DTO;

public class HeatMap
{
    public int VisitFrequency { get; set; }
    public LocationDto Location { get; set; }
}