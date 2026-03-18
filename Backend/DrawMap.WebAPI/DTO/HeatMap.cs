using DrawMap.Domain;

namespace DrawMap.WebAPI.DTO;

public class HeatMap
{
    public float VisitFrequency { get; set; }
    public LocationDto Location { get; set; }
}