using DrawMap.WebAPI.DTO;

namespace DrawMap.WebAPI.Services;

public interface IHeatMapService
{
    Task<List<HeatMap>> GetHeatMap(CancellationToken cancellationToken);
}